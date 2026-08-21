package com.mdmac.organizer.ui.planner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mdmac.organizer.data.planner.PlannerEntry
import com.mdmac.organizer.databinding.ItemPlannerEntryBinding
import com.mdmac.organizer.util.DateRangeUtils

class PlannerEntryAdapter(
    private val onClick: (PlannerEntry) -> Unit,
    private val onLongClick: (PlannerEntry) -> Boolean
) : ListAdapter<PlannerEntry, PlannerEntryAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemPlannerEntryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPlannerEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val entry = getItem(position)
        holder.binding.entryTitle.text = entry.title
        holder.binding.entryTime.text = DateRangeUtils.formatEntryTime(entry.dateTimeMillis)
        if (entry.notes.isNotBlank()) {
            holder.binding.entryNotes.visibility = View.VISIBLE
            holder.binding.entryNotes.text = entry.notes
        } else {
            holder.binding.entryNotes.visibility = View.GONE
        }
        holder.itemView.setOnClickListener { onClick(entry) }
        holder.itemView.setOnLongClickListener { onLongClick(entry) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<PlannerEntry>() {
            override fun areItemsTheSame(old: PlannerEntry, new: PlannerEntry) = old.id == new.id
            override fun areContentsTheSame(old: PlannerEntry, new: PlannerEntry) = old == new
        }
    }
}
