package com.blockick.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blockick.app.data.preferences.AppPreferences
import com.blockick.app.vpn.VpnController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var vpnController: VpnController

    @Inject
    lateinit var appPreferences: AppPreferences

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            scope.launch {
                val isEnabled = appPreferences.vpnEnabled.first()
                if (isEnabled) {
                    vpnController.start()
                }
            }
        }
    }
}

