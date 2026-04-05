package com.blockick.app.vpn

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.blockick.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class VpnTileService : TileService() {

    @Inject
    lateinit var vpnController: VpnController

    private var statusJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onStartListening() {
        super.onStartListening()
        statusJob = vpnController.status
            .onEach { status ->
                updateTile(status)
            }
            .launchIn(serviceScope)
    }

    override fun onStopListening() {
        super.onStopListening()
        statusJob?.cancel()
    }

    override fun onClick() {
        super.onClick()
        val currentStatus = vpnController.status.value
        
        if (currentStatus == VpnStatus.RUNNING) {
            vpnController.stop()
        } else if (currentStatus == VpnStatus.STOPPED) {
            if (vpnController.needsPermission()) {
                // If permission is needed, we should probably open the app
                val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        // On Android 14+, we use startByPendingIntent if possible, but for simplicity:
                        startActivityAndCollapse(intent)
                    } else {
                        @Suppress("DEPRECATION")
                        startActivityAndCollapse(intent)
                    }
                }
            } else {
                vpnController.start()
            }
        }
    }

    private fun updateTile(status: VpnStatus) {
        val tile = qsTile ?: return
        
        when (status) {
            VpnStatus.RUNNING -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = getString(R.string.status_active)
            }
            VpnStatus.STARTING -> {
                tile.state = Tile.STATE_INACTIVE // Or some busy state if supported
                tile.label = getString(R.string.status_starting)
            }
            VpnStatus.STOPPED -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = getString(R.string.status_inactive)
            }
        }
        
        tile.updateTile()
    }

    override fun onDestroy() {
        super.onDestroy()
        statusJob?.cancel()
    }
}
