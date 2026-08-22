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
    private val _selectedDayMillis = MutableStateFlow(System.currentTimeMillis())

    val anchorMillis: StateFlow<Long> = _anchorMillis
    val mode: StateFlow<PlannerViewMode> = _mode
    val selectedDayMillis: StateFlow<Long> = _selectedDayMillis

    /**
     * Entries shown in the bottom list. Day and Week show their whole
     * range; Month shows only the currently selected day (the calendar
     * grid up top is what narrows it down).
     */
    val entries: StateFlow<List<PlannerEntry>> =
        combine(_anchorMillis, _mode, _selectedDayMillis) { anchor, mode, selectedDay ->
            Triple(anchor, mode, selectedDay)
        }
            .flatMapLatest { (anchor, mode, selectedDay) ->
                val (start, end) = when (mode) {
                    PlannerViewMode.DAY -> DateRangeUtils.dayRange(anchor)
                    PlannerViewMode.WEEK -> DateRangeUtils.weekRange(anchor)
                    PlannerViewMode.MONTH -> DateRangeUtils.dayRange(selectedDay)
                }
                repository.getBetween(start, end)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Whole visible month's entries, used only to compute calendar-cell dots. */
    val monthEntries: StateFlow<List<PlannerEntry>> =
        combine(_anchorMillis, _mode) { anchor, mode -> anchor to mode }
            .flatMapLatest { (anchor, mode) ->
                if (mode == PlannerViewMode.MONTH) {
                    val (start, end) = DateRangeUtils.monthRange(anchor)
                    repository.getBetween(start, end)
                } else {
                    flowOf(emptyList())
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setMode(newMode: PlannerViewMode) {
        _mode.value = newMode
        if (newMode == PlannerViewMode.MONTH) {
            _selectedDayMillis.value = defaultSelectedDay(_anchorMillis.value)
        }
    }

    fun shift(forward: Boolean) {
        _anchorMillis.value = DateRangeUtils.shift(_anchorMillis.value, _mode.value, forward)
        if (_mode.value == PlannerViewMode.MONTH) {
            _selectedDayMillis.value = defaultSelectedDay(_anchorMillis.value)
        }
    }

    fun selectDay(dayMillis: Long) {
        _selectedDayMillis.value = dayMillis
    }

    fun jumpToToday() {
        val now = System.currentTimeMillis()
        _anchorMillis.value = now
        _selectedDayMillis.value = now
    }

    fun save(entry: PlannerEntry) = viewModelScope.launch {
        if (entry.id == 0L) repository.insert(entry) else repository.update(entry)
    }

    fun delete(entry: PlannerEntry) = viewModelScope.launch { repository.delete(entry) }

    /** "Today" if it falls within the visible month, otherwise the 1st of that month. */
    private fun defaultSelectedDay(anchor: Long): Long {
        val (monthStart, monthEnd) = DateRangeUtils.monthRange(anchor)
        val today = System.currentTimeMillis()
        return if (today in monthStart..monthEnd) today else monthStart
    }

    class Factory(private val repository: PlannerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PlannerViewModel(repository) as T
    }
}
