package com.mdmac.organizer.ui.apps

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mdmac.organizer.data.apps.InstalledApp
import com.mdmac.organizer.databinding.DialogHiddenAppsBinding
import com.mdmac.organizer.databinding.ItemHiddenAppBinding

/**
 * Shows the current hidden apps with an Unhide action for each. Unhiding an
 * app removes it from this list immediately without closing the dialog, so
 * multiple apps can be unhidden in one session.
 */
class HiddenAppsDialog(
    initialHiddenApps: List<InstalledApp>,
    private val onUnhide: (InstalledApp) -> Unit
) : DialogFragment() {

    private val remaining = initialHiddenApps.toMutableList()
    private var binding: DialogHiddenAppsBinding? = null
    private lateinit var adapter: HiddenAppAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogHiddenAppsBinding.inflate(LayoutInflater.from(requireContext()))
        this.binding = binding

        adapter = HiddenAppAdapter { app -> handleUnhide(app) }
        binding.hiddenAppsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.hiddenAppsRecyclerView.adapter = adapter
        refreshList()

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton("Close", null)
            .create()
    }

    private fun handleUnhide(app: InstalledApp) {
        onUnhide(app)
        remaining.removeAll { it.packageName == app.packageName }
        refreshList()
    }

    private fun refreshList() {
        adapter.submitList(remaining.toList())
        binding?.hiddenEmptyView?.visibility = if (remaining.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

private class HiddenAppAdapter(
    private val onUnhideClick: (InstalledApp) -> Unit
) : ListAdapter<InstalledApp, HiddenAppAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemHiddenAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHiddenAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = getItem(position)
        holder.binding.hiddenAppIcon.setImageDrawable(app.icon)
        holder.binding.hiddenAppLabel.text = app.label
        holder.binding.unhideButton.setOnClickListener { onUnhideClick(app) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<InstalledApp>() {
            override fun areItemsTheSame(old: InstalledApp, new: InstalledApp) =
                old.packageName == new.packageName

            override fun areContentsTheSame(old: InstalledApp, new: InstalledApp) = old == new
        }
    }
}
