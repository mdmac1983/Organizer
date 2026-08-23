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

/**
 * Owner-only screen: grid/dock size sliders for Owner's own Home, plus a
 * checklist of installed apps used to curate what Guest sees on theirs.
 * (Owner's own home-app selection reuses the same checklist mechanism,
 * switched via the toggle at the top.)
 */
class HomeScreenSettingsActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeScreenSettingsBinding
    private lateinit var homeRepository: HomeRepository
    private lateinit var appsRepository: AppsRepository
    private var editingProfile: Profile = Profile.OWNER
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
            editingProfile = if (id == binding.btnEditGuest.id) Profile.GUEST else Profile.OWNER
            refreshChecklist()
        }
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

        // apply-on-release: only commit the value in onStopTrackingTouch, per spec
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

    private fun refreshChecklist() {
        binding.appChecklistContainer.removeAllViews()
        val selected = homeRepository.getHomePackages(editingProfile).toMutableSet()

        allApps.forEach { app ->
            val itemBinding = ItemAppChecklistBinding.inflate(layoutInflater, binding.appChecklistContainer, false)
            itemBinding.appLabel.text = app.label
            itemBinding.appIcon.setImageDrawable(app.icon)
            itemBinding.checkbox.isChecked = app.packageName in selected
            itemBinding.checkbox.setOnCheckedChangeListener { _, checked ->
                if (checked) selected.add(app.packageName) else selected.remove(app.packageName)
                homeRepository.setHomePackages(editingProfile, selected)
            }
            binding.appChecklistContainer.addView(itemBinding.root)
        }
    }
}
