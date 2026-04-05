package com.blockick.app.ui.screens.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockick.app.data.db.dao.AllowlistDao
import com.blockick.app.data.db.dao.UserBlocklistDao
import com.blockick.app.data.db.entities.AllowlistEntity
import com.blockick.app.data.db.entities.UserBlocklistEntity
import com.blockick.app.domain.engine.BlocklistEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalRulesViewModel @Inject constructor(
    private val allowlistDao: AllowlistDao,
    private val userBlocklistDao: UserBlocklistDao,
    private val blocklistEngine: BlocklistEngine
) : ViewModel() {

    val allowedDomains: StateFlow<List<AllowlistEntity>> = allowlistDao.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val blockedDomains: StateFlow<List<UserBlocklistEntity>> = userBlocklistDao.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun removeAllowedDomain(domain: String) {
        viewModelScope.launch {
            allowlistDao.delete(AllowlistEntity(domain))
            blocklistEngine.loadRules()
        }
    }

    fun removeBlockedDomain(domain: String) {
        viewModelScope.launch {
            userBlocklistDao.delete(UserBlocklistEntity(domain))
            blocklistEngine.loadRules()
        }
    }
}

