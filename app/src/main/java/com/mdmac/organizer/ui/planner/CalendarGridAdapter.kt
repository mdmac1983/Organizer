package com.mdmac.organizer.ui.planner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.R as MaterialR
import com.google.android.material.color.MaterialColors
import com.mdmac.organizer.R
import com.mdmac.organizer.databinding.ItemCalendarDayBinding
import com.mdmac.organizer.util.DateRangeUtils

class CalendarGridAdapter(
    private val onDayClick: (CalendarDay) -> Unit
) : ListAdapter<CalendarDay, CalendarGridAdapter.VH>(DIFF) {

    var selectedDayMillis: Long = System.currentTimeMillis()
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    var daysWithEntries: Set<Long> = emptySet()
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    inner class VH(val binding: ItemCalendarDayBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val day = getItem(position)

        holder.binding.dayNumber.text = day.dayOfMonth.toString()
        holder.binding.dayNumber.alpha = if (day.isCurrentMonth) 1f else 0.35f

        val isSelected = DateRangeUtils.isSameDay(day.dateMillis, selectedDayMillis)
        val isToday = DateRangeUtils.isSameDay(day.dateMillis, System.currentTimeMillis())

        holder.binding.dayNumber.setBackgroundResource(
            when {
                isSelected -> R.drawable.bg_calendar_day_selected
                isToday -> R.drawable.bg_calendar_day_today
                else -> 0
            }
        )
        holder.binding.dayNumber.setTextColor(
            if (isSelected) {
                MaterialColors.getColor(holder.itemView, MaterialR.attr.colorOnPrimary)
            } else {
                MaterialColors.getColor(holder.itemView, MaterialR.attr.colorOnSurface)
            }
        )

        holder.binding.entryDot.visibility =
            if (daysWithEntries.any { DateRangeUtils.isSameDay(it, day.dateMillis) }) {
                View.VISIBLE
            } else {
                View.GONE
            }

        holder.itemView.setOnClickListener { onDayClick(day) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CalendarDay>() {
            override fun areItemsTheSame(old: CalendarDay, new: CalendarDay) = old.dateMillis == new.dateMillis
            override fun areContentsTheSame(old: CalendarDay, new: CalendarDay) = old == new
        }
    }
}
