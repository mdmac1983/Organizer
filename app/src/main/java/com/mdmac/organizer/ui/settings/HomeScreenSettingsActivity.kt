package com.mdmac.organizer.ui.settings

import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.mdmac.organizer.data.apps.AppsRepository
import com.mdmac.organizer.data.apps.InstalledApp
import com.mdmac.organizer.data.home.HomeRepository
import com.mdmac.organizer.databinding.ActivityHomeScreenSettingsBinding
import com.mdmac.organizer.databinding.ItemAppChecklistBinding
import com.mdmac.organizer.profile.Profile
import com.mdmac.organizer.theme.BaseActivity
import kotlinx.coroutines.launch

private enum class EditTarget { OWNER_HOME, GUEST_HOME, GUEST_DRAWER }

class HomeScreenSettingsActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeScreenSettingsBinding
    private lateinit var homeRepository: HomeRepository
    private lateinit var appsRepository: AppsRepository
    private var editTarget: EditTarget = EditTarget.OWNER_HOME
    private var allApps: List<InstalledApp> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        homeRepository = HomeRepository(this)
        appsRepository = AppsRepository(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupSliders()
        binding.profileToggleGroup.addOnButtonCheckedListener { _, id, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            editTarget = when (id) {
                binding.btnEditGuest.id -> EditTarget.GUEST_HOME
                binding.btnEditGuestDrawer.id -> EditTarget.GUEST_DRAWER
                else -> EditTarget.OWNER_HOME
            }
            refreshChecklist()
        }
        binding.btnSelectAll.setOnClickListener { setAllChecked(true) }
        binding.btnSelectNone.setOnClickListener { setAllChecked(false) }
        loadApps()
    }

    private fun setupSliders() {
        binding.columnsSeekBar.max = HomeRepository.OWNER_COLUMNS_MAX - HomeRepository.OWNER_COLUMNS_MIN
        binding.rowsSeekBar.max = HomeRepository.OWNER_ROWS_MAX - HomeRepository.OWNER_ROWS_MIN
        binding.dockSeekBar.max = HomeRepository.OWNER_DOCK_MAX - HomeRepository.OWNER_DOCK_MIN

        binding.columnsSeekBar.progress = homeRepository.getColumns(Profile.OWNER) - HomeRepository.OWNER_COLUMNS_MIN
        binding.rowsSeekBar.progress = homeRepository.getRows(Profile.OWNER) - HomeRepository.OWNER_ROWS_MIN
        binding.dockSeekBar.progress = homeRepository.getDockSlots(Profile.OWNER) - HomeRepository.OWNER_DOCK_MIN

        updateSliderLabels()

        binding.columnsSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = updateSliderLabels()
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val requested = HomeRepository.OWNER_COLUMNS_MIN + (seekBar?.progress ?: 0)
                if (!homeRepository.setOwnerColumns(requested)) {
                    Toast.makeText(this@HomeScreenSettingsActivity, "Clear space to shrink further", Toast.LENGTH_SHORT).show()
                    seekBar?.progress = homeRepository.getColumns(Profile.OWNER) - HomeRepository.OWNER_COLUMNS_MIN
                }
                updateSliderLabels()
            }
        })
        binding.rowsSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = updateSliderLabels()
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val requested = HomeRepository.OWNER_ROWS_MIN + (seekBar?.progress ?: 0)
                if (!homeRepository.setOwnerRows(requested)) {
                    Toast.makeText(this@HomeScreenSettingsActivity, "Clear space to shrink further", Toast.LENGTH_SHORT).show()
                    seekBar?.progress = homeRepository.getRows(Profile.OWNER) - HomeRepository.OWNER_ROWS_MIN
                }
                updateSliderLabels()
            }
        })
        binding.dockSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = updateSliderLabels()
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val requested = HomeRepository.OWNER_DOCK_MIN + (seekBar?.progress ?: 0)
                if (!homeRepository.setOwnerDockSlots(requested)) {
                    Toast.makeText(this@HomeScreenSettingsActivity, "Clear space to shrink further", Toast.LENGTH_SHORT).show()
                    seekBar?.progress = homeRepository.getDockSlots(Profile.OWNER) - HomeRepository.OWNER_DOCK_MIN
                }
                updateSliderLabels()
            }
        })
    }

    private fun updateSliderLabels() {
        binding.columnsValue.text = (HomeRepository.OWNER_COLUMNS_MIN + binding.columnsSeekBar.progress).toString()
        binding.rowsValue.text = (HomeRepository.OWNER_ROWS_MIN + binding.rowsSeekBar.progress).toString()
        binding.dockValue.text = (HomeRepository.OWNER_DOCK_MIN + binding.dockSeekBar.progress).toString()
    }

    private fun loadApps() {
        lifecycleScope.launch {
            allApps = appsRepository.getLaunchableApps()
            refreshChecklist()
        }
    }

    private fun currentSelection(): MutableSet<String> = when (editTarget) {
        EditTarget.OWNER_HOME -> homeRepository.getHomePackages(Profile.OWNER).toMutableSet()
        EditTarget.GUEST_HOME -> homeRepository.getHomePackages(Profile.GUEST).toMutableSet()
        EditTarget.GUEST_DRAWER -> homeRepository.getGuestDrawerPackages().toMutableSet()
    }

    private fun saveSelection(selected: Set<String>) {
        when (editTarget) {
            EditTarget.OWNER_HOME -> homeRepository.setHomePackages(Profile.OWNER, selected)
            EditTarget.GUEST_HOME -> homeRepository.setHomePackages(Profile.GUEST, selected)
            EditTarget.GUEST_DRAWER -> homeRepository.setGuestDrawerPackages(selected)
        }
    }

    private fun refreshChecklist() {
        binding.appChecklistContainer.removeAllViews()
        val selected = currentSelection()

        allApps.forEach { app ->
            val itemBinding = ItemAppChecklistBinding.inflate(layoutInflater, binding.appChecklistContainer, false)
            itemBinding.appLabel.text = app.label
            itemBinding.appIcon.setImageDrawable(app.icon)
            itemBinding.checkbox.isChecked = app.packageName in selected
            itemBinding.checkbox.setOnCheckedChangeListener { switchView, checked ->
                if (switchView.isPressed) {
                    val current = currentSelection()
                    if (checked) current.add(app.packageName) else current.remove(app.packageName)
                    saveSelection(current)
                }
            }
            binding.appChecklistContainer.addView(itemBinding.root)
        }
    }

    private fun setAllChecked(checked: Boolean) {
        val newSelection = if (checked) allApps.map { it.packageName }.toSet() else emptySet()
        saveSelection(newSelection)
        refreshChecklist()
    }
}
