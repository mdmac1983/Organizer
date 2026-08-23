package com.mdmac.organizer.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mdmac.organizer.data.apps.AppsRepository
import com.mdmac.organizer.data.apps.InstalledApp
import com.mdmac.organizer.data.home.HomeRepository
import com.mdmac.organizer.databinding.ActivityHomeBinding
import com.mdmac.organizer.profile.Profile
import com.mdmac.organizer.profile.ProfileManager
import com.mdmac.organizer.theme.BaseActivity
import com.mdmac.organizer.ui.apps.AppGridAdapter
import com.mdmac.organizer.ui.apps.AppGridItem
import com.mdmac.organizer.ui.settings.SettingsActivity
import kotlinx.coroutines.launch

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var profileManager: ProfileManager
    private lateinit var homeRepository: HomeRepository
    private lateinit var appsRepository: AppsRepository
    private lateinit var scaleDetector: ScaleGestureDetector

    private lateinit var gridAdapter: AppGridAdapter
    private lateinit var dockAdapter: AppGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileManager = ProfileManager(this)
        homeRepository = HomeRepository(this)
        appsRepository = AppsRepository(this)

        setupGrids()
        setupPinchGesture()
        setupLongPress()
        loadHome()
    }

    override fun onResume() {
        super.onResume()
        loadHome()
    }

    private fun setupGrids() {
        gridAdapter = AppGridAdapter(
            onAppClick = ::launchApp,
            onAppLongClick = { _, _ -> false },
            onHiddenFolderClick = {}
        )
        dockAdapter = AppGridAdapter(
            onAppClick = ::launchApp,
            onAppLongClick = { _, _ -> false },
            onHiddenFolderClick = {}
        )
        binding.homeGridRecyclerView.adapter = gridAdapter
        binding.homeDockRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.homeDockRecyclerView.adapter = dockAdapter
    }

    private fun loadHome() {
        val profile = profileManager.getCurrentProfile()
        val columns = homeRepository.getColumns(profile)

        binding.homeGridRecyclerView.layoutManager = GridLayoutManager(this, columns)

        lifecycleScope.launch {
            val homeApps = homeRepository.getHomeApps(profile)
            gridAdapter.submitList(homeApps.map { AppGridItem.AppEntry(it) })

            val dockPackages = appsRepository.getPinnedPackages()
            val allApps = appsRepository.getLaunchableApps()
            val dockApps = allApps.filter { it.packageName in dockPackages }
                .take(homeRepository.getDockSlots(profile))
            binding.homeDockRecyclerView.visibility = if (dockApps.isEmpty()) View.GONE else View.VISIBLE
            dockAdapter.submitList(dockApps.map { AppGridItem.AppEntry(it) })
        }
    }

    private fun launchApp(app: InstalledApp) {
        packageManager.getLaunchIntentForPackage(app.packageName)?.let { startActivity(it) }
    }

    // --- Pinch-out: toggles Guest <-> Owner in both directions ---

    private fun setupPinchGesture() {
        scaleDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private var handled = false

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                handled = false
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                if (!handled && detector.scaleFactor > PINCH_OUT_THRESHOLD) {
                    handled = true
                    profileManager.toggle()
                    recreate()
                }
                return true
            }
        })
        binding.root.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                binding.root.performClick()
            }
            true
        }
    }

    // --- Long-press: Wallpapers only (Guest) / Wallpapers + Home settings (Owner) ---
    // Wallpaper picker itself lands in Batch C — this wires the popup shape now
    // so Batch C only has to fill in the "Wallpapers" action.

    private fun setupLongPress() {
        binding.root.setOnLongClickListener {
            showHomeLongPressPopup()
            true
        }
    }

    private fun showHomeLongPressPopup() {
        val popup = androidx.appcompat.widget.PopupMenu(this, binding.root)
        popup.menu.add(0, MENU_WALLPAPERS, 0, "Wallpapers")
        if (profileManager.getCurrentProfile() == Profile.OWNER) {
            popup.menu.add(0, MENU_HOME_SETTINGS, 1, "Home settings")
        }
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_WALLPAPERS -> {
                    // TODO Batch C: launch wallpaper picker
                    true
                }
                MENU_HOME_SETTINGS -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    companion object {
        private const val PINCH_OUT_THRESHOLD = 1.15f
        private const val MENU_WALLPAPERS = 1
        private const val MENU_HOME_SETTINGS = 2
    }
}
