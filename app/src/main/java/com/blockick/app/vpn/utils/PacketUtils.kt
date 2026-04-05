package com.blockick.app.vpn.utils

import java.nio.ByteBuffer

object PacketUtils {
    
    fun buildUdpPacket(
        ipSrc: ByteArray,
        ipDest: ByteArray,
        portSrc: Int,
        portDest: Int,
        payload: ByteArray
    ): ByteArray {
        val udpLen = 8 + payload.size
        val ipLen = 20 + udpLen
        val buffer = ByteBuffer.allocate(ipLen)

        // IP Header (20 bytes)
        buffer.put(0x45.toByte()) // Version + IHL
        buffer.put(0x00.toByte()) // ToS
        buffer.putShort(ipLen.toShort()) // Total Length
        buffer.putShort(0.toShort()) // ID
        buffer.putShort(0x4000.toShort()) // Flags (Don't Fragment)
        buffer.put(0x40.toByte()) // TTL
        buffer.put(17.toByte()) // Protocol (UDP)
        buffer.putShort(0.toShort()) // Checksum (to be calculated)
        buffer.put(ipSrc)
        buffer.put(ipDest)

        // Calculate IP Checksum
        val ipHeader = buffer.array().copyOf(20)
        buffer.putShort(10, calculateChecksum(ipHeader))

        // UDP Header (8 bytes)
        buffer.putShort(portSrc.toShort())
        buffer.putShort(portDest.toShort())
        buffer.putShort(udpLen.toShort())
        buffer.putShort(0.toShort()) // Checksum (0 = optional in IPv4)

        // Payload
        buffer.put(payload)

        return buffer.array()
    }

    fun buildIPv6UdpPacket(
        ipSrc: ByteArray,
        ipDest: ByteArray,
        portSrc: Int,
        portDest: Int,
        payload: ByteArray
    ): ByteArray {
        val udpLen = 8 + payload.size
        val ipLen = 40 + udpLen
        val buffer = ByteBuffer.allocate(ipLen)

        // IPv6 Header (40 bytes)
        // Version (4 bits), Traffic Class (8 bits), Flow Label (20 bits)
        buffer.putInt(0x60000000.toInt()) 
        buffer.putShort(udpLen.toShort()) // Payload Length (UDP header + payload)
        buffer.put(17.toByte()) // Next Header (UDP)
        buffer.put(64.toByte()) // Hop Limit
        buffer.put(ipSrc) // Source Address (16 bytes)
        buffer.put(ipDest) // Destination Address (16 bytes)

        // UDP Header (8 bytes)
        buffer.putShort(portSrc.toShort())
        buffer.putShort(portDest.toShort())
        buffer.putShort(udpLen.toShort())
        buffer.putShort(0.toShort()) // Checksum (0 = invalid in IPv6, but often ignored in local tunnels)
        // Future: Calculate proper IPv6 UDP checksum using pseudo-header if needed

        // Payload
        buffer.put(payload)

        return buffer.array()
    }

    private fun calculateChecksum(data: ByteArray): Short {
        var sum = 0
        var i = 0
        while (i < data.size - 1) {
            sum += ((data[i].toInt() and 0xFF) shl 8) or (data[i + 1].toInt() and 0xFF)
            i += 2
        }
        if (i < data.size) {
            sum += (data[i].toInt() and 0xFF) shl 8
        }
        while (sum shr 16 != 0) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        return (sum.inv() and 0xFFFF).toShort()
    }
}

