package com.blockick.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockick.app.data.preferences.AppPreferences
import com.blockick.app.worker.WorkManagerScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val workManagerScheduler: WorkManagerScheduler
) : ViewModel() {

    val upstreamDns = appPreferences.upstreamDns
    val autoUpdate = appPreferences.autoUpdate
    val updateFrequency = appPreferences.updateFrequency
    val safeSearchEnabled = appPreferences.safeSearchEnabled
    val bypassEnabled = appPreferences.bypassEnabled
    val bypassStartHour = appPreferences.bypassStartHour
    val bypassStartMinute = appPreferences.bypassStartMinute
    val bypassEndHour = appPreferences.bypassEndHour
    val bypassEndMinute = appPreferences.bypassEndMinute
    val bypassDays = appPreferences.bypassDays

    fun setAutoUpdate(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setAutoUpdate(enabled)
            workManagerScheduler.scheduleOrCancel()
        }
    }

    fun setUpdateFrequency(frequency: Int) {
        viewModelScope.launch {
            appPreferences.setUpdateFrequency(frequency)
            workManagerScheduler.scheduleOrCancel()
        }
    }

    fun setSafeSearchEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setSafeSearchEnabled(enabled)
        }
    }

    fun setUpstreamDns(dns: String) {
        viewModelScope.launch {
            appPreferences.setUpstreamDns(dns)
        }
    }

    fun setBypassEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setBypassEnabled(enabled)
        }
    }

    fun setBypassStartTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            appPreferences.setBypassStartTime(hour, minute)
        }
    }

    fun setBypassEndTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            appPreferences.setBypassEndTime(hour, minute)
        }
    }

    fun setBypassDays(days: String) {
        viewModelScope.launch {
            appPreferences.setBypassDays(days)
        }
    }
}

