package com.mdmac.organizer.ui.gestures

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.mdmac.organizer.data.apps.AppsRepository
import com.mdmac.organizer.databinding.ActivityGesturesSettingsBinding
import com.mdmac.organizer.databinding.ItemSettingsRowBinding
import com.mdmac.organizer.gestures.GestureAction
import com.mdmac.organizer.gestures.GesturePreference
import com.mdmac.organizer.gestures.GestureType
import com.mdmac.organizer.theme.BaseActivity
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.launch

class GesturesSettingsActivity : BaseActivity() {

    private lateinit var binding: ActivityGesturesSettingsBinding
    private lateinit var gesturePreference: GesturePreference
    private lateinit var appsRepository: AppsRepository
    private val rowBindings = mutableMapOf<GestureType, ItemSettingsRowBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGesturesSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gesturePreference = GesturePreference(this)
        appsRepository = AppsRepository(this)

        binding.toolbar.setNavigationOnClickListener { finish() }
        buildRows()
    }

    private fun buildRows() {
        GestureType.entries.forEach { type ->
            val rowBinding = ItemSettingsRowBinding.inflate(LayoutInflater.from(this), binding.rowContainer, false)
            rowBinding.rowTitle.text = type.label
            rowBinding.rowIcon.backgroundTintList =
                androidx.core.content.ContextCompat.getColorStateList(this, com.mdmac.organizer.R.color.row_icon_gestures)
            rowBinding.root.setOnClickListener { showActionPicker(type) }
            binding.rowContainer.addView(rowBinding.root)
            rowBindings[type] = rowBinding
        }
        refreshSubtitles()
    }

    private fun refreshSubtitles() {
        rowBindings.forEach { (type, rowBinding) ->
            val action = gesturePreference.getAction(type)
            rowBinding.rowSubtitle.text = if (action == GestureAction.LAUNCH_APP) {
                "Launch app"
            } else {
                action.label
            }
        }
    }

    private fun showActionPicker(type: GestureType) {
        val actions = GestureAction.entries.toTypedArray()
        val labels = actions.map { it.label }.toTypedArray()
        val currentIndex = actions.indexOf(gesturePreference.getAction(type))

        AlertDialog.Builder(this)
            .setTitle(type.label)
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                val chosen = actions[which]
                gesturePreference.setAction(type, chosen)
                dialog.dismiss()
                if (chosen == GestureAction.LAUNCH_APP) {
                    showAppPicker(type)
                } else {
                    refreshSubtitles()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAppPicker(type: GestureType) {
        lifecycleScope.launch {
            val apps = appsRepository.getLaunchableApps()
            val labels = apps.map { it.label }.toTypedArray()
            AlertDialog.Builder(this@GesturesSettingsActivity)
                .setTitle("Choose app")
                .setItems(labels) { _, which ->
                    gesturePreference.setLaunchPackage(type, apps[which].packageName)
                    refreshSubtitles()
                }
                .show()
        }
    }
}
