package com.blockick.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockick.app.data.db.dao.StatsDao
import com.blockick.app.data.db.dao.QueryLogDao
import com.blockick.app.data.db.entities.QueryLogEntity
import com.blockick.app.data.repository.BlocklistRepository
import com.blockick.app.domain.engine.BlocklistEngine
import com.blockick.app.vpn.VpnController
import com.blockick.app.vpn.VpnStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import android.util.Log
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val vpnController: VpnController,
    private val statsDao: StatsDao,
    private val queryLogDao: QueryLogDao,
    private val blocklistRepository: BlocklistRepository,
    private val blocklistEngine: BlocklistEngine
) : ViewModel() {

    val vpnStatus = vpnController.status

    val blockedToday: Flow<Int> = statsDao.getLast7Days().map { stats ->
        val todayStr = LocalDate.now().toString()
        stats.find { it.date == todayStr }?.blockedCount ?: 0
    }

    val blockedWeekly: Flow<Int> = statsDao.getLast7Days().map { stats ->
        stats.sumOf { it.blockedCount }
    }

    val blockedTotal: Flow<Int> = statsDao.getTotalBlocked().map { it ?: 0 }

    val hourlyStats = queryLogDao.getHourlyStats(System.currentTimeMillis() - 24 * 3600000)

    val recentBlockedLogs = queryLogDao.getRecentLogs()
        .map { logs -> logs.filter { it.isBlocked }.take(5) }

    private val _activeProfile = MutableStateFlow("Balanced")
    val activeProfile: StateFlow<String> = _activeProfile.asStateFlow()

    val rulesCount: StateFlow<Int> = blocklistRepository.getTotalRulesCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    fun toggleVpn() {
        Log.d("HomeViewModel", "toggleVpn() called. Current status: ${vpnStatus.value}")
        when (vpnStatus.value) {
            VpnStatus.RUNNING, VpnStatus.STARTING -> {
                vpnController.stop()
            }
            VpnStatus.STOPPED -> {
                if (vpnController.needsPermission()) {
                    viewModelScope.launch {
                        _events.emit(HomeEvent.RequestVpnPermission)
                    }
                } else {
                    vpnController.start()
                }
            }
        }
    }

    fun setProfile(profileName: String) {
        viewModelScope.launch {
            _activeProfile.value = profileName
            _events.emit(HomeEvent.Loading(true))
            blocklistRepository.applyProfile(profileName)
            _events.emit(HomeEvent.Loading(false))
        }
    }

    fun updateAll() {
        viewModelScope.launch {
            _events.emit(HomeEvent.Loading(true))
            blocklistRepository.refreshAllWithUrls()
            _events.emit(HomeEvent.Loading(false))
        }
    }
}

sealed class HomeEvent {
    object RequestVpnPermission : HomeEvent()
    data class Loading(val isLoading: Boolean) : HomeEvent()
}
