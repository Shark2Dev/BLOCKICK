package com.blockick.app.ui.screens.activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockick.app.data.db.dao.AllowlistDao
import com.blockick.app.data.db.dao.QueryLogDao
import com.blockick.app.data.db.dao.UserBlocklistDao
import com.blockick.app.data.db.entities.AllowlistEntity
import com.blockick.app.data.db.entities.UserBlocklistEntity
import com.blockick.app.domain.engine.BlocklistEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivityDetailUiState(
    val domain: String = "",
    val queryType: String = "A",
    val occurrenceCount: Int = 0,
    val isAllowed: Boolean = false,
    val isBlocked: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val queryLogDao: QueryLogDao,
    private val allowlistDao: AllowlistDao,
    private val userBlocklistDao: UserBlocklistDao,
    private val blocklistEngine: BlocklistEngine
) : ViewModel() {

    private val rawDomain: String = checkNotNull(savedStateHandle["domain"])
    private val domain: String = rawDomain.lowercase().trimEnd('.')
    
    private val _uiState = MutableStateFlow(ActivityDetailUiState(domain = domain))
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val count = queryLogDao.getOccurrenceCount(domain)
            val type = queryLogDao.getLastQueryType(domain) ?: "A"
            val allowed = allowlistDao.isAllowed(domain)
            val userBlocked = userBlocklistDao.isBlocked(domain)
            
            // PRIORITY LOGIC:
            // 1. If in explicit allowlist -> STATUS: ALLOWED (Green)
            // 2. If in explicit user blocklist -> STATUS: BLOCKED (Primary)
            // 3. Otherwise, use engine history (was it blocked during the query?)
            
            val blocked = when {
                allowed -> false
                userBlocked -> true
                else -> {
                    // Fallback to what actually happened in the logs
                    queryLogDao.isDomainBlocked(domain) ?: false
                }
            }
            
            _uiState.value = _uiState.value.copy(
                occurrenceCount = count,
                queryType = type,
                isAllowed = allowed,
                isBlocked = blocked,
                isLoading = false
            )
        }
    }

    fun toggleAllow() {
        viewModelScope.launch {
            if (_uiState.value.isAllowed) {
                allowlistDao.delete(AllowlistEntity(domain))
            } else {
                allowlistDao.insert(AllowlistEntity(domain))
                userBlocklistDao.delete(UserBlocklistEntity(domain))
            }
            blocklistEngine.loadRules() 
            loadData()
        }
    }

    fun toggleBlock() {
        viewModelScope.launch {
            // If currently allowed or historical allow, adding to blocklist forces blocked
            if (_uiState.value.isBlocked && userBlocklistDao.isBlocked(domain)) {
                userBlocklistDao.delete(UserBlocklistEntity(domain))
            } else {
                userBlocklistDao.insert(UserBlocklistEntity(domain))
                allowlistDao.delete(AllowlistEntity(domain))
            }
            blocklistEngine.loadRules()
            loadData()
        }
    }
}

