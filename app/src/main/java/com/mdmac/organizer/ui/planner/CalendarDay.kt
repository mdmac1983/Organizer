package com.mdmac.organizer.ui.planner

data class CalendarDay(
    val dateMillis: Long,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean
)
