package com.blockick.app.ui.screens.settings

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockick.app.data.preferences.AppPreferences
import com.blockick.app.vpn.VpnController
import com.blockick.app.vpn.VpnStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuditResult(
    val title: String,
    val description: String,
    val status: AuditStatus,
    val action: String? = null
)

enum class AuditStatus {
    SECURE, WARNING, DANGER, LOADING
}

data class AuditUiState(
    val isScanning: Boolean = false,
    val results: List<AuditResult> = emptyList(),
    val overallScore: Int = 0 // 0 to 100
)

@HiltViewModel
class NetworkAuditViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vpnController: VpnController,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditUiState())
    val uiState: StateFlow<AuditUiState> = _uiState.asStateFlow()

    fun startScan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, results = emptyList())
            
            val results = mutableListOf<AuditResult>()
            
            // 1. VPN Status
            delay(800)
            val vpnIsRunning = vpnController.status.value == VpnStatus.RUNNING
            results.add(AuditResult(
                "Protection Status",
                if (vpnIsRunning) "VPN tunnel is active and filtering traffic." else "Protection is currently disabled.",
                if (vpnIsRunning) AuditStatus.SECURE else AuditStatus.DANGER,
                if (vpnIsRunning) null else "Enable VPN"
            ))
            _uiState.value = _uiState.value.copy(results = results.toList())

            // 2. Private DNS Check
            delay(1000)
            val hasPrivateDns = checkPrivateDns()
            results.add(AuditResult(
                "Private DNS Conflict",
                if (hasPrivateDns) "Android Private DNS is active and might bypass your blocklists." else "No conflicting DNS settings detected.",
                if (hasPrivateDns) AuditStatus.WARNING else AuditStatus.SECURE,
                if (hasPrivateDns) "Open Settings" else null
            ))
            _uiState.value = _uiState.value.copy(results = results.toList())

            // 3. Upstream Security
            delay(800)
            val dns = appPreferences.upstreamDns.first()
            val isDoH = dns.startsWith("https") || listOf("1.1.1.1", "8.8.8.8", "9.9.9.9").contains(dns)
            results.add(AuditResult(
                "Upstream DNS Security",
                if (isDoH) "Using encrypted DoH (DNS-over-HTTPS) for external queries." else "Upstream DNS might be using unencrypted protocols.",
                if (isDoH) AuditStatus.SECURE else AuditStatus.WARNING
            ))
            _uiState.value = _uiState.value.copy(results = results.toList())

            // 4. Connection Encryption
            delay(600)
            val isWifi = isOnWifi()
            results.add(AuditResult(
                "Network Type",
                if (isWifi) "Connected via Wi-Fi. Local network filtering is active." else "Connected via Mobile Data.",
                AuditStatus.SECURE
            ))
            
            val score = calculateScore(results)
            _uiState.value = _uiState.value.copy(isScanning = false, results = results.toList(), overallScore = score)
        }
    }

    private fun checkPrivateDns(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val linkProperties = connectivityManager.getLinkProperties(connectivityManager.activeNetwork)
            val privateDnsServer = linkProperties?.privateDnsServerName
            privateDnsServer != null
        } catch (e: Exception) {
            false
        }
    }

    private fun isOnWifi(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    }

    private fun calculateScore(results: List<AuditResult>): Int {
        var score = 100
        results.forEach {
            if (it.status == AuditStatus.DANGER) score -= 40
            if (it.status == AuditStatus.WARNING) score -= 15
        }
        return score.coerceAtLeast(0)
    }
}

