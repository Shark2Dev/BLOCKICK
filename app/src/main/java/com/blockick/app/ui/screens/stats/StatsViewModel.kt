package com.blockick.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import com.blockick.app.data.db.dao.QueryLogDao
import com.blockick.app.data.db.dao.StatsDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.viewModelScope
import com.blockick.app.data.db.dao.AllowlistDao
import com.blockick.app.data.db.dao.UserBlocklistDao
import com.blockick.app.data.db.entities.AllowlistEntity
import com.blockick.app.data.db.entities.UserBlocklistEntity
import com.blockick.app.domain.engine.BlocklistEngine
import kotlinx.coroutines.launch

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val queryLogDao: QueryLogDao,
    private val statsDao: StatsDao,
    private val allowlistDao: AllowlistDao,
    private val userBlocklistDao: UserBlocklistDao,
    private val blocklistEngine: BlocklistEngine
) : ViewModel() {

    val recentLogs = queryLogDao.getRecentLogs()
    val weeklyStats = statsDao.getLast7Days()
    val topBlockedDomains = queryLogDao.getTopBlockedDomains(5)

    fun toggleBlock(domain: String, currentBlocked: Boolean) {
        viewModelScope.launch {
            if (currentBlocked) {
                // Remove from user blocklist
                userBlocklistDao.delete(UserBlocklistEntity(domain))
            } else {
                // Add to user blocklist
                userBlocklistDao.insert(UserBlocklistEntity(domain))
                // Ensure it's not in allowlist
                allowlistDao.delete(AllowlistEntity(domain))
            }
            blocklistEngine.loadRules()
        }
    }

    fun toggleAllow(domain: String, currentAllowed: Boolean) {
        viewModelScope.launch {
            if (currentAllowed) {
                // Remove from allowlist
                allowlistDao.delete(AllowlistEntity(domain))
            } else {
                // Add to allowlist
                allowlistDao.insert(AllowlistEntity(domain))
                // Ensure it's not in user blocklist
                userBlocklistDao.delete(UserBlocklistEntity(domain))
            }
            blocklistEngine.loadRules()
        }
    }
}

