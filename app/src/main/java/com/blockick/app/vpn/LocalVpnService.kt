package com.blockick.app.vpn

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.blockick.app.data.db.dao.ExcludedAppDao
import com.blockick.app.domain.engine.BlocklistEngine
import com.blockick.app.domain.engine.DnsPacketProcessor
import com.blockick.app.vpn.utils.PacketUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetAddress
import java.nio.ByteBuffer
import javax.inject.Inject

@AndroidEntryPoint
class LocalVpnService : VpnService() {

    @Inject
    lateinit var dnsPacketProcessor: DnsPacketProcessor

    @Inject
    lateinit var blocklistEngine: BlocklistEngine

    @Inject
    lateinit var excludedAppDao: ExcludedAppDao

    @Inject
    lateinit var vpnController: VpnController

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val packetChannel = kotlinx.coroutines.channels.Channel<ByteArray>(capacity = 1000)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("VpnService", "Starting VPN Service")
        vpnController.setStatus(VpnStatus.STARTING)
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        if (vpnInterface != null) return

        serviceScope.launch {
            // Ensure rules are loaded before processing packets
            blocklistEngine.loadRules()

            val excludedApps = excludedAppDao.getExcludedPackageNames()
            Log.d("VpnService", "Excluding ${excludedApps.size} apps from VPN")

            val localIp = "10.1.1.2"
            val vpnDns = "10.1.1.1"
            
            // IPv6 addresses
            val localIp6 = "fd00:1:1:1::2"
            val vpnDns6 = "fd00:1:1:1::1"

            val builder = Builder()
                .setSession("BLOCKICK")
                .addAddress(localIp, 32)
                .addDnsServer(vpnDns)
                .addRoute(vpnDns, 32)
                
                // Add IPv6 support to prevent leaks
                .addAddress(localIp6, 128)
                .addDnsServer(vpnDns6)
                .addRoute(vpnDns6, 128)

                // Intercept common public DNS servers to prevent bypass
                .addRoute("8.8.8.8", 32)
                .addRoute("8.8.4.4", 32)
                .addRoute("1.1.1.1", 32)
                .addRoute("1.0.0.1", 32)
                .addRoute("9.9.9.9", 32)
                .addRoute("208.67.222.222", 32)
                
                .addDisallowedApplication(packageName) // Always exclude itself

            // Add user-defined exclusions
            excludedApps.forEach { pkg ->
                try {
                    builder.addDisallowedApplication(pkg)
                } catch (e: Exception) {
                    Log.e("VpnService", "Could not exclude package: $pkg", e)
                }
            }

            builder.setBlocking(true)

            try {
                vpnInterface = builder.establish()
                Log.i("VpnService", "VPN Interface established")
                vpnController.setStatus(VpnStatus.RUNNING)
            } catch (e: Exception) {
                Log.e("VpnService", "Failed to establish VPN interface", e)
                vpnController.setStatus(VpnStatus.STOPPED)
                stopSelf()
                return@launch
            }

            try {
                vpnInterface?.let { pfd ->
                    val fd = pfd.fileDescriptor
                    val inputStream = FileInputStream(fd)
                    val outputStream = FileOutputStream(fd)

                    // Start Worker Pool
                    repeat(4) {
                        launch {
                            for (packet in packetChannel) {
                                processPacket(packet, outputStream)
                            }
                        }
                    }

                    val buffer = ByteBuffer.allocate(32767)

                    while (isActive) {
                        try {
                            val length = inputStream.read(buffer.array())
                            if (length > 0) {
                                val packetData = buffer.array().copyOf(length)
                                packetChannel.trySend(packetData)
                                buffer.clear()
                            } else if (length < 0) {
                                Log.i("VpnService", "End of stream reached")
                                break
                            }
                        } catch (e: Exception) {
                            Log.e("VpnService", "Error reading from VPN interface", e)
                            break
                        }
                    }
                }
            } finally {
                Log.i("VpnService", "VPN loop finished. Cleaning up.")
                vpnInterface?.close()
                vpnInterface = null
                vpnController.setStatus(VpnStatus.STOPPED)
                stopSelf()
            }
        }
    }

    private suspend fun processPacket(packet: ByteArray, outputStream: FileOutputStream) {
        if (packet.size < 20) return
        val version = (packet[0].toInt() shr 4) and 0x0F
        
        // IPv4 Handling
        if (version == 4) {
            handleIPv4Packet(packet, outputStream)
        } else if (version == 6) {
            handleIPv6Packet(packet, outputStream)
        }
    }

    private suspend fun handleIPv6Packet(packet: ByteArray, outputStream: FileOutputStream) {
        if (packet.size < 48) return // Minimum IPv6 (40) + UDP (8)
        
        val nextHeader = packet[6].toInt()
        if (nextHeader == 17) { // UDP
            val udpHeaderStart = 40
            val srcPort = ((packet[udpHeaderStart].toInt() and 0xFF) shl 8) or (packet[udpHeaderStart + 1].toInt() and 0xFF)
            val destPort = ((packet[udpHeaderStart + 2].toInt() and 0xFF) shl 8) or (packet[udpHeaderStart + 3].toInt() and 0xFF)

            if (destPort == 53) {
                val dnsDataStart = udpHeaderStart + 8
                val dnsLength = packet.size - dnsDataStart
                if (dnsLength > 0) {
                    val dnsQuery = packet.copyOfRange(dnsDataStart, packet.size)
                    val responseBytes = dnsPacketProcessor.processPacket(dnsQuery)
                    
                    if (responseBytes != null) {
                        val srcIp = packet.copyOfRange(8, 24)
                        val destIp = packet.copyOfRange(24, 40)
                        
                        // Swap IPs and Ports for response
                        val responsePacket = PacketUtils.buildIPv6UdpPacket(
                            ipSrc = destIp,
                            ipDest = srcIp,
                            portSrc = destPort,
                            portDest = srcPort,
                            payload = responseBytes
                        )
                        
                        try {
                            synchronized(outputStream) {
                                outputStream.write(responsePacket)
                            }
                        } catch (e: Exception) {
                            Log.e("VpnService", "Error writing VPN response (IPv6)", e)
                        }
                    }
                }
            }
        }
    }

    private suspend fun handleIPv4Packet(packet: ByteArray, outputStream: FileOutputStream) {
        val protocol = packet[9].toInt()
        val ihl = (packet[0].toInt() and 0x0F) * 4
        
        if (protocol == 17) { // UDP
            val udpHeaderStart = ihl
            val srcPort = ((packet[udpHeaderStart].toInt() and 0xFF) shl 8) or (packet[udpHeaderStart + 1].toInt() and 0xFF)
            val destPort = ((packet[udpHeaderStart + 2].toInt() and 0xFF) shl 8) or (packet[udpHeaderStart + 3].toInt() and 0xFF)

            if (destPort == 53) {
                val dnsDataStart = udpHeaderStart + 8
                val dnsLength = packet.size - dnsDataStart
                if (dnsLength > 0) {
                    val dnsQuery = packet.copyOfRange(dnsDataStart, packet.size)
                    val responseBytes = dnsPacketProcessor.processPacket(dnsQuery)
                    
                    if (responseBytes != null) {
                        val srcIp = packet.copyOfRange(12, 16)
                        val destIp = packet.copyOfRange(16, 20)
                        
                        // Swap IPs and Ports for response
                        val responsePacket = PacketUtils.buildUdpPacket(
                            ipSrc = destIp,
                            ipDest = srcIp,
                            portSrc = destPort,
                            portDest = srcPort,
                            payload = responseBytes
                        )
                        
                        try {
                            synchronized(outputStream) {
                                outputStream.write(responsePacket)
                            }
                        } catch (e: Exception) {
                            Log.e("VpnService", "Error writing VPN response", e)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        vpnInterface?.close()
        vpnInterface = null
        vpnController.setStatus(VpnStatus.STOPPED)
    }
}

