package com.mdmac.organizer.ui.planner

import androidx.lifecycle.*
import com.mdmac.organizer.data.planner.PlannerEntry
import com.mdmac.organizer.data.planner.PlannerRepository
import com.mdmac.organizer.util.DateRangeUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlannerViewModel(private val repository: PlannerRepository) : ViewModel() {

    private val _anchorMillis = MutableStateFlow(System.currentTimeMillis())
    private val _mode = MutableStateFlow(PlannerViewMode.WEEK)

    val anchorMillis: StateFlow<Long> = _anchorMillis
    val mode: StateFlow<PlannerViewMode> = _mode

    val entries: StateFlow<List<PlannerEntry>> =
        combine(_anchorMillis, _mode) { anchor, mode -> anchor to mode }
            .flatMapLatest { (anchor, mode) ->
                val (start, end) = when (mode) {
                    PlannerViewMode.DAY -> DateRangeUtils.dayRange(anchor)
                    PlannerViewMode.WEEK -> DateRangeUtils.weekRange(anchor)
                    PlannerViewMode.MONTH -> DateRangeUtils.monthRange(anchor)
                }
                repository.getBetween(start, end)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setMode(newMode: PlannerViewMode) { _mode.value = newMode }

    fun shift(forward: Boolean) {
        _anchorMillis.value = DateRangeUtils.shift(_anchorMillis.value, _mode.value, forward)
    }

    fun jumpToToday() { _anchorMillis.value = System.currentTimeMillis() }

    fun save(entry: PlannerEntry) = viewModelScope.launch {
        if (entry.id == 0L) repository.insert(entry) else repository.update(entry)
    }

    fun delete(entry: PlannerEntry) = viewModelScope.launch { repository.delete(entry) }

    class Factory(private val repository: PlannerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PlannerViewModel(repository) as T
    }
}
