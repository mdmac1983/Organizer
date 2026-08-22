package com.mdmac.organizer.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateRangeUtils {

    fun dayRange(anchorMillis: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = anchorMillis }
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        return start to cal.timeInMillis - 1
    }

    fun weekRange(anchorMillis: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = anchorMillis }
        // Always start the week on Sunday, regardless of device locale
        // (locale default is used elsewhere and isn't reliably Sunday).
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 7)
        return start to cal.timeInMillis - 1
    }

    fun monthRange(anchorMillis: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = anchorMillis }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        return start to cal.timeInMillis - 1
    }

    fun shift(anchorMillis: Long, mode: com.mdmac.organizer.ui.planner.PlannerViewMode, forward: Boolean): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = anchorMillis }
        val amount = if (forward) 1 else -1
        when (mode) {
            com.mdmac.organizer.ui.planner.PlannerViewMode.DAY -> cal.add(Calendar.DAY_OF_MONTH, amount)
            com.mdmac.organizer.ui.planner.PlannerViewMode.WEEK -> cal.add(Calendar.WEEK_OF_YEAR, amount)
            com.mdmac.organizer.ui.planner.PlannerViewMode.MONTH -> cal.add(Calendar.MONTH, amount)
        }
        return cal.timeInMillis
    }

    fun label(anchorMillis: Long, mode: com.mdmac.organizer.ui.planner.PlannerViewMode): String {
        val cal = Calendar.getInstance().apply { timeInMillis = anchorMillis }
        return when (mode) {
            com.mdmac.organizer.ui.planner.PlannerViewMode.DAY ->
                SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(cal.time)
            com.mdmac.organizer.ui.planner.PlannerViewMode.WEEK -> {
                val (start, end) = weekRange(anchorMillis)
                val startFmt = SimpleDateFormat("MMM d", Locale.getDefault()).format(start)
                val endFmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(end)
                "$startFmt – $endFmt"
            }
            com.mdmac.organizer.ui.planner.PlannerViewMode.MONTH ->
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
        }
    }

    fun formatEntryTime(millis: Long): String =
        SimpleDateFormat("EEE, MMM d — h:mm a", Locale.getDefault()).format(millis)

    /** Time only, no date — used in Day mode where every entry is already the same day. */
    fun formatTimeOnly(millis: Long): String =
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(millis)

    fun isSameDay(a: Long, b: Long): Boolean {
        val calA = Calendar.getInstance().apply { timeInMillis = a }
        val calB = Calendar.getInstance().apply { timeInMillis = b }
        return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
            calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Full calendar grid for the month containing [anchorMillis]: complete
     * Sunday-through-Saturday weeks, including the leading/trailing days
     * from adjacent months needed to fill the grid (5 or 6 rows of 7).
     */
    fun monthGridDays(anchorMillis: Long): List<com.mdmac.organizer.ui.planner.CalendarDay> {
        val targetCal = Calendar.getInstance().apply { timeInMillis = anchorMillis }
        val targetMonth = targetCal.get(Calendar.MONTH)
        val targetYear = targetCal.get(Calendar.YEAR)

        val gridStart = Calendar.getInstance().apply {
            timeInMillis = anchorMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) add(Calendar.DAY_OF_MONTH, -1)
        }

        val gridEnd = Calendar.getInstance().apply {
            timeInMillis = anchorMillis
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            while (get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) add(Calendar.DAY_OF_MONTH, 1)
        }

        val days = mutableListOf<com.mdmac.organizer.ui.planner.CalendarDay>()
        val cursor = gridStart.clone() as Calendar
        while (!cursor.after(gridEnd)) {
            val isCurrentMonth = cursor.get(Calendar.MONTH) == targetMonth && cursor.get(Calendar.YEAR) == targetYear
            days.add(
                com.mdmac.organizer.ui.planner.CalendarDay(
                    dateMillis = cursor.timeInMillis,
                    dayOfMonth = cursor.get(Calendar.DAY_OF_MONTH),
                    isCurrentMonth = isCurrentMonth
                )
            )
            cursor.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }
}
