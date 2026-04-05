package com.blockick.app.vpn

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log
import com.blockick.app.data.preferences.AppPreferences
import com.blockick.app.ui.widget.VpnWidgetProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

enum class VpnStatus {
    STOPPED, STARTING, RUNNING
}

@Singleton
class VpnController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences
) {
    private val _status = MutableStateFlow(VpnStatus.STOPPED)
    val status: StateFlow<VpnStatus> = _status
    
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    init {
        // Automatically restart the service if it's supposed to be enabled according to preferences
        scope.launch {
            try {
                val isEnabled = appPreferences.vpnEnabled.first()
                if (isEnabled && _status.value == VpnStatus.STOPPED) {
                    if (!needsPermission()) {
                        Log.d("VpnController", "Automatically restarting VPN from init")
                        start()
                    } else {
                        // If permission is missing, we can't start automatically. 
                        // The user will need to trigger it manually from the UI.
                        Log.d("VpnController", "VPN enabled in preferences but missing permission. Doing nothing.")
                    }
                }
            } catch (e: Exception) {
                Log.e("VpnController", "Failed to restore status", e)
            }
        }
    }

    fun needsPermission(): Boolean {
        return VpnService.prepare(context) != null
    }

    fun start() {
        Log.i("VpnController", "start() called. Current status: ${_status.value}")
        if (_status.value != VpnStatus.STOPPED) {
            Log.d("VpnController", "VPN is already starting or running. Ignoring start request.")
            return
        }
        
        scope.launch {
            // Set preference FIRST to ensure widget reads 'true' when it updates
            appPreferences.setVpnEnabled(true)
            withContext(Dispatchers.Main.immediate) {
                setStatus(VpnStatus.STARTING)
                val intent = Intent(context, LocalVpnService::class.java)
                context.startService(intent)
            }
        }
    }

    fun stop() {
        Log.i("VpnController", "stop() called. Current status: ${_status.value}")
        if (_status.value == VpnStatus.STOPPED) return
        
        scope.launch {
            appPreferences.setVpnEnabled(false)
            withContext(Dispatchers.Main.immediate) {
                setStatus(VpnStatus.STOPPED)
                val intent = Intent(context, LocalVpnService::class.java)
                context.stopService(intent)
            }
        }
    }

    fun setStatus(newStatus: VpnStatus) {
        if (_status.value != newStatus) {
            Log.d("VpnController", "Status changing: ${_status.value} -> $newStatus")
            _status.value = newStatus
            updateWidgets()
        }
    }

    fun updateWidgets() {
        scope.launch {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, VpnWidgetProvider::class.java)
                val ids = appWidgetManager.getAppWidgetIds(componentName)
                if (ids != null && ids.isNotEmpty()) {
                    Log.d("VpnController", "Triggering widget update for ids: ${ids.joinToString()}")
                    val intent = Intent(context, VpnWidgetProvider::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                    context.sendBroadcast(intent)
                }
            } catch (e: Exception) {
                Log.e("VpnController", "Failed to update widgets", e)
            }
        }
    }
}
