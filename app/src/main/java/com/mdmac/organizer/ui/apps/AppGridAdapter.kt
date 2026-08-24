package com.mdmac.organizer.ui.apps

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mdmac.organizer.R
import com.mdmac.organizer.data.apps.InstalledApp
import com.mdmac.organizer.databinding.ItemAppIconBinding

sealed class AppGridItem {
    data class AppEntry(val app: InstalledApp) : AppGridItem()
    data class HiddenFolder(val count: Int) : AppGridItem()
}

private const val HIDDEN_FOLDER_STABLE_ID = "__hidden_folder__"

private fun AppGridItem.stableId(): String = when (this) {
    is AppGridItem.AppEntry -> this.app.packageName
    is AppGridItem.HiddenFolder -> HIDDEN_FOLDER_STABLE_ID
}

/**
 * fixedItemWidthDp: for horizontal LinearLayoutManager (docks) — see earlier note.
 * useHomeLabelStyle: forces white text + a drop shadow, independent of theme —
 * for Home screen instances, where labels sit over an arbitrary wallpaper rather
 * than a solid surface, so theme-driven text color can't guarantee contrast.
 */
class AppGridAdapter(
    private val onAppClick: (InstalledApp) -> Unit,
    private val onAppLongClick: (InstalledApp, View) -> Boolean,
    private val onHiddenFolderClick: () -> Unit,
    private val fixedItemWidthDp: Int? = null,
    private val useHomeLabelStyle: Boolean = false
) : ListAdapter<AppGridItem, AppGridAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemAppIconBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAppIconBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        fixedItemWidthDp?.let { widthDp ->
            val widthPx = (widthDp * parent.context.resources.displayMetrics.density).toInt()
            binding.root.layoutParams = binding.root.layoutParams.apply { width = widthPx }
        }
        if (useHomeLabelStyle) {
            binding.appLabel.setTextColor(Color.WHITE)
            binding.appLabel.setShadowLayer(4f, 0f, 1f, Color.argb(180, 0, 0, 0))
        }
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        when (val item = getItem(position)) {
            is AppGridItem.AppEntry -> {
                val app = item.app
                holder.binding.appIcon.setImageDrawable(app.icon)
                holder.binding.appLabel.text = app.label
                holder.itemView.setOnClickListener { onAppClick(app) }
                holder.itemView.setOnLongClickListener { onAppLongClick(app, holder.itemView) }
            }
            is AppGridItem.HiddenFolder -> {
                holder.binding.appIcon.setImageResource(R.drawable.ic_hidden_folder)
                holder.binding.appLabel.text = holder.itemView.context.getString(
                    R.string.hidden_folder_label, item.count
                )
                holder.itemView.setOnClickListener { onHiddenFolderClick() }
                holder.itemView.setOnLongClickListener { false }
            }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<AppGridItem>() {
            override fun areItemsTheSame(old: AppGridItem, new: AppGridItem) =
                old.stableId() == new.stableId()

            override fun areContentsTheSame(old: AppGridItem, new: AppGridItem) = old == new
        }
    }
}
