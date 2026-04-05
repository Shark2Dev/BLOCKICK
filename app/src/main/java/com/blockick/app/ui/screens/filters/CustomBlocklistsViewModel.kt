package com.blockick.app.ui.screens.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockick.app.data.db.entities.CustomListEntity
import com.blockick.app.data.repository.BlocklistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomBlocklistsViewModel @Inject constructor(
    private val repository: BlocklistRepository
) : ViewModel() {

    val customLists = repository.customLists

    fun addCustomList(name: String, url: String, format: String) {
        viewModelScope.launch {
            repository.addCustomList(name, url, format)
        }
    }

    fun removeCustomList(list: CustomListEntity) {
        viewModelScope.launch {
            repository.removeCustomList(list)
        }
    }

    fun toggleCustomList(list: CustomListEntity) {
        viewModelScope.launch {
            repository.toggleCustomList(list)
        }
    }
}

