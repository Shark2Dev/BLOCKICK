package com.blockick.app.ui

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.blockick.app.ui.screens.main.MainScreen
import com.blockick.app.ui.theme.BlockickTheme
import com.blockick.app.ui.screens.home.HomeEvent
import com.blockick.app.ui.screens.home.HomeViewModel
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Permission granted, you could trigger a state update here if needed
            // HomeViewModel already observes the controller state.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            BlockickTheme {
                MainScreen()
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d("MainActivity", "Starting HomeViewModel event collection")
                homeViewModel.events.collect { event ->
                    Log.d("MainActivity", "Received event: $event")
                    when (event) {
                        is HomeEvent.RequestVpnPermission -> requestVpnPermission()
                        is HomeEvent.Loading -> { /* Handled in UI */ }
                    }
                }
            }
        }
    }

    fun requestVpnPermission() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        }
    }
}

