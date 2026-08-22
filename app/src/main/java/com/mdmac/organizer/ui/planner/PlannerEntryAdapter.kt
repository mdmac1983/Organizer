package com.mdmac.organizer.ui.planner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mdmac.organizer.data.planner.PlannerEntry
import com.mdmac.organizer.databinding.ItemPlannerEntryBinding
import com.mdmac.organizer.databinding.ItemPlannerEntryBulletBinding
import com.mdmac.organizer.util.DateRangeUtils

class PlannerEntryAdapter(
    private val onClick: (PlannerEntry) -> Unit,
    private val onLongClick: (PlannerEntry) -> Boolean
) : ListAdapter<PlannerEntry, RecyclerView.ViewHolder>(DIFF) {

    var bulletStyle: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                notifyItemRangeChanged(0, itemCount)
            }
        }

    inner class CardVH(val binding: ItemPlannerEntryBinding) : RecyclerView.ViewHolder(binding.root)
    inner class BulletVH(val binding: ItemPlannerEntryBulletBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int = if (bulletStyle) VIEW_TYPE_BULLET else VIEW_TYPE_CARD

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_BULLET) {
            BulletVH(ItemPlannerEntryBulletBinding.inflate(inflater, parent, false))
        } else {
            CardVH(ItemPlannerEntryBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val entry = getItem(position)
        when (holder) {
            is CardVH -> bindCard(holder, entry)
            is BulletVH -> bindBullet(holder, entry)
        }
    }

    private fun bindCard(holder: CardVH, entry: PlannerEntry) {
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

    private fun bindBullet(holder: BulletVH, entry: PlannerEntry) {
        holder.binding.entryTitle.text = entry.title
        holder.binding.entryTime.text = DateRangeUtils.formatTimeOnly(entry.dateTimeMillis)
        holder.itemView.setOnClickListener { onClick(entry) }
        holder.itemView.setOnLongClickListener { onLongClick(entry) }
    }

    companion object {
        private const val VIEW_TYPE_CARD = 0
        private const val VIEW_TYPE_BULLET = 1

        val DIFF = object : DiffUtil.ItemCallback<PlannerEntry>() {
            override fun areItemsTheSame(old: PlannerEntry, new: PlannerEntry) = old.id == new.id
            override fun areContentsTheSame(old: PlannerEntry, new: PlannerEntry) = old == new
        }
    }
}
