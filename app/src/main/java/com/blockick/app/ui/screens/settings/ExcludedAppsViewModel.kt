package com.blockick.app.ui.screens.settings

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockick.app.data.db.dao.ExcludedAppDao
import com.blockick.app.data.db.entities.ExcludedAppEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AppInfo(
    val packageName: String,
    val appName: String,
    val isExcluded: Boolean,
    val isSystemApp: Boolean,
    val icon: Drawable? = null
)

@HiltViewModel
class ExcludedAppsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val excludedAppDao: ExcludedAppDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    
    val uiState: StateFlow<List<AppInfo>> = combine(
        _installedApps,
        excludedAppDao.getAll(),
        _searchQuery
    ) { installed, excluded, query ->
        val excludedPackageNames = excluded.map { it.packageName }.toSet()
        installed.map { app ->
            app.copy(isExcluded = excludedPackageNames.contains(app.packageName))
        }.filter { 
            it.appName.contains(query, ignoreCase = true) || 
            it.packageName.contains(query, ignoreCase = true)
        }.sortedBy { it.appName }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                packages.map { app ->
                    AppInfo(
                        packageName = app.packageName,
                        appName = pm.getApplicationLabel(app).toString(),
                        isExcluded = false,
                        isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                        icon = try { pm.getApplicationIcon(app.packageName) } catch (e: Exception) { null }
                    )
                }.filter { it.packageName != context.packageName }
            }
            _installedApps.value = apps
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleExclusion(app: AppInfo) {
        viewModelScope.launch {
            if (app.isExcluded) {
                excludedAppDao.delete(ExcludedAppEntity(app.packageName, app.appName))
            } else {
                excludedAppDao.insert(ExcludedAppEntity(app.packageName, app.appName))
            }
        }
    }
}

