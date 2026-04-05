package com.blockick.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vpnEnabledKey = booleanPreferencesKey("vpn_enabled")
    private val upstreamDnsKey = stringPreferencesKey("upstream_dns")
    private val autoUpdateKey = booleanPreferencesKey("auto_update")
    private val updateFrequencyKey = intPreferencesKey("update_frequency")
    private val safeSearchEnabledKey = booleanPreferencesKey("safe_search")
    private val bypassEnabledKey = booleanPreferencesKey("bypass_enabled")
    private val bypassStartHourKey = intPreferencesKey("bypass_start_hour")
    private val bypassStartMinuteKey = intPreferencesKey("bypass_start_minute")
    private val bypassEndHourKey = intPreferencesKey("bypass_end_hour")
    private val bypassEndMinuteKey = intPreferencesKey("bypass_end_minute")
    private val bypassDaysKey = stringPreferencesKey("bypass_days")

    val vpnEnabled: Flow<Boolean> = context.dataStore.data.map { it[vpnEnabledKey] ?: false }
    val upstreamDns: Flow<String> = context.dataStore.data.map { it[upstreamDnsKey] ?: "1.1.1.1" }
    val autoUpdate: Flow<Boolean> = context.dataStore.data.map { it[autoUpdateKey] ?: true }
    val updateFrequency: Flow<Int> = context.dataStore.data.map { it[updateFrequencyKey] ?: 1 }
    val safeSearchEnabled: Flow<Boolean> = context.dataStore.data.map { it[safeSearchEnabledKey] ?: false }
    val bypassEnabled: Flow<Boolean> = context.dataStore.data.map { it[bypassEnabledKey] ?: false }
    val bypassStartHour: Flow<Int> = context.dataStore.data.map { it[bypassStartHourKey] ?: 2 }
    val bypassStartMinute: Flow<Int> = context.dataStore.data.map { it[bypassStartMinuteKey] ?: 0 }
    val bypassEndHour: Flow<Int> = context.dataStore.data.map { it[bypassEndHourKey] ?: 3 }
    val bypassEndMinute: Flow<Int> = context.dataStore.data.map { it[bypassEndMinuteKey] ?: 0 }
    val bypassDays: Flow<String> = context.dataStore.data.map { it[bypassDaysKey] ?: "1,2,3,4,5,6,7" }

    suspend fun setVpnEnabled(enabled: Boolean) {
        context.dataStore.edit { it[vpnEnabledKey] = enabled }
    }

    suspend fun setUpstreamDns(dns: String) {
        context.dataStore.edit { it[upstreamDnsKey] = dns }
    }

    suspend fun setAutoUpdate(enabled: Boolean) {
        context.dataStore.edit { it[autoUpdateKey] = enabled }
    }

    suspend fun setUpdateFrequency(frequency: Int) {
        context.dataStore.edit { it[updateFrequencyKey] = frequency }
    }

    suspend fun setSafeSearchEnabled(enabled: Boolean) {
        context.dataStore.edit { it[safeSearchEnabledKey] = enabled }
    }

    suspend fun setBypassEnabled(enabled: Boolean) {
        context.dataStore.edit { it[bypassEnabledKey] = enabled }
    }

    suspend fun setBypassStartTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[bypassStartHourKey] = hour
            it[bypassStartMinuteKey] = minute
        }
    }

    suspend fun setBypassEndTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[bypassEndHourKey] = hour
            it[bypassEndMinuteKey] = minute
        }
    }

    suspend fun setBypassDays(days: String) {
        context.dataStore.edit { it[bypassDaysKey] = days }
    }
}

