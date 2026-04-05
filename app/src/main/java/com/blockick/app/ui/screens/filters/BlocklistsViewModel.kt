package com.blockick.app.ui.screens.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockick.app.data.db.dao.BlocklistDao
import com.blockick.app.data.db.entities.BlocklistEntity
import com.blockick.app.data.repository.BlocklistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlocklistsViewModel @Inject constructor(
    private val blocklistDao: BlocklistDao,
    private val repository: BlocklistRepository
) : ViewModel() {

    val blocklists = blocklistDao.getAll()

    fun toggleList(list: BlocklistEntity) {
        viewModelScope.launch {
            val allLists = blocklistDao.getAll().first()
            val newState = !list.isEnabled
            
            // 1. Update the list itself
            blocklistDao.update(list.copy(isEnabled = newState))
            
            // 2. If it's a parent, update all its children to the same state
            if (list.parentId == null && list.url.isEmpty()) {
                allLists.filter { it.parentId == list.id }.forEach { child ->
                    blocklistDao.update(child.copy(isEnabled = newState))
                }
            }
            
            // 3. If it's a child and being enabled, disable other siblings (Mutual Exclusion if desired)
            // Note: Keeping mutual exclusion for groups like OISD where profiles are alternatives
            if (newState && list.parentId != null) {
                allLists.filter { it.parentId == list.parentId && it.id != list.id && it.isEnabled }.forEach { sibling ->
                    blocklistDao.update(sibling.copy(isEnabled = false))
                }
            }
            
            // Trigger background update if enabled
            if (newState && list.url.isNotEmpty()) {
                repository.refreshAllWithUrls()
            }
        }
    }

    fun updateAll() {
        viewModelScope.launch {
            repository.refreshAllWithUrls()
        }
    }
}

