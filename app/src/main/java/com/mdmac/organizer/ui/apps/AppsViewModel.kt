package com.mdmac.organizer.ui.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mdmac.organizer.data.apps.AppsRepository
import com.mdmac.organizer.data.apps.InstalledApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppsViewModel(private val repository: AppsRepository) : ViewModel() {

    private val allApps = MutableStateFlow<List<InstalledApp>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** Visible grid apps: not hidden, filtered by the current search query. */
    val gridApps: StateFlow<List<InstalledApp>> = combine(allApps, _searchQuery) { apps, query ->
        apps.filter { !it.isHidden && it.label.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Pinned apps for the bottom dock, capped at the configured slot count. */
    val dockApps: StateFlow<List<InstalledApp>> = allApps.map { apps ->
        apps.filter { it.isPinned }.take(repository.getDockSlotCount())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hiddenApps: StateFlow<List<InstalledApp>> = allApps.map { apps ->
        apps.filter { it.isHidden }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            allApps.value = repository.getLaunchableApps()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun togglePin(app: InstalledApp) {
        repository.setPinned(app.packageName, !app.isPinned)
        refresh()
    }

    fun setHidden(app: InstalledApp, hidden: Boolean) {
        repository.setHidden(app.packageName, hidden)
        refresh()
    }

    class Factory(private val repository: AppsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AppsViewModel(repository) as T
    }
}
