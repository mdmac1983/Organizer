package com.mdmac.organizer.ui.home

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mdmac.organizer.data.apps.AppsRepository
import com.mdmac.organizer.data.apps.InstalledApp
import com.mdmac.organizer.data.home.HomeRepository
import com.mdmac.organizer.data.wallpaper.WallpaperRepository
import com.mdmac.organizer.databinding.ActivityHomeBinding
import com.mdmac.organizer.gestures.GestureAction
import com.mdmac.organizer.gestures.GestureExecutor
import com.mdmac.organizer.gestures.GesturePreference
import com.mdmac.organizer.gestures.GestureType
import com.mdmac.organizer.profile.Profile
import com.mdmac.organizer.profile.ProfileManager
import com.mdmac.organizer.theme.BaseActivity
import com.mdmac.organizer.ui.apps.AppGridAdapter
import com.mdmac.organizer.ui.apps.AppGridItem
import com.mdmac.organizer.ui.settings.SettingsActivity
import com.mdmac.organizer.ui.wallpaper.WallpaperPickerActivity
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var profileManager: ProfileManager
    private lateinit var homeRepository: HomeRepository
    private lateinit var appsRepository: AppsRepository
    private lateinit var wallpaperRepository: WallpaperRepository
    private lateinit var gesturePreference: GesturePreference
    private lateinit var gestureExecutor: GestureExecutor

    private lateinit var scaleDetector: ScaleGestureDetector
    private lateinit var gestureDetector: GestureDetector

    private lateinit var gridAdapter: AppGridAdapter
    private lateinit var dockAdapter: AppGridAdapter

    private var drawerOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileManager = ProfileManager(this)
        homeRepository = HomeRepository(this)
        appsRepository = AppsRepository(this)
        wallpaperRepository = WallpaperRepository(this)
        gesturePreference = GesturePreference(this)
        gestureExecutor = GestureExecutor(this)

        setupGrids()
        setupGestures()
        setupLongPress()
        loadWallpaper()
        loadHome()
    }

    override fun onResume() {
        super.onResume()
        loadWallpaper()
        loadHome()
    }

    override fun onBackPressed() {
        if (drawerOpen) {
            closeDrawer()
        } else {
            super.onBackPressed()
        }
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

    private fun loadWallpaper() {
        val customPath = wallpaperRepository.getCustomUri()
        val bundledRes = wallpaperRepository.getBundledResId()
        when {
            customPath != null && File(customPath).exists() -> {
                val bitmap = BitmapFactory.decodeFile(customPath)
                binding.wallpaperImageView.setImageBitmap(bitmap)
            }
            bundledRes != null -> binding.wallpaperImageView.setImageResource(bundledRes)
            else -> binding.wallpaperImageView.setImageDrawable(null)
        }
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

    // --- Drawer overlay (app drawer opens by sliding AppsFragment up over Home) ---

    private fun openDrawer() {
        if (drawerOpen) return
        drawerOpen = true
        binding.drawerContainer.visibility = View.VISIBLE
        binding.drawerContainer.translationY = binding.drawerContainer.height.toFloat().coerceAtLeast(2000f)
        binding.drawerContainer.animate().translationY(0f).setDuration(220).start()
    }

    private fun closeDrawer() {
        if (!drawerOpen) return
        binding.drawerContainer.animate()
            .translationY(binding.drawerContainer.height.toFloat().coerceAtLeast(2000f))
            .setDuration(200)
            .withEndAction {
                binding.drawerContainer.visibility = View.GONE
                drawerOpen = false
            }
            .start()
    }

    // --- Gestures: pinch-out (profile toggle) + swipe/double-tap/pinch-in (mappable) ---

    private fun setupGestures() {
        scaleDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private var handled = false

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                handled = false
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                if (handled) return true
                if (detector.scaleFactor > PINCH_OUT_THRESHOLD) {
                    handled = true
                    profileManager.toggle()
                    recreate()
                } else if (detector.scaleFactor < PINCH_IN_THRESHOLD) {
                    handled = true
                    runGestureAction(GestureType.PINCH_IN)
                }
                return true
            }
        })

        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                runGestureAction(GestureType.DOUBLE_TAP)
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null) return false
                val dx = e2.x - e1.x
                val dy = e2.y - e1.y
                if (abs(dx) < SWIPE_MIN_DISTANCE && abs(dy) < SWIPE_MIN_DISTANCE) return false

                if (abs(dx) > abs(dy)) {
                    if (dx > 0) runGestureAction(GestureType.SWIPE_RIGHT) else runGestureAction(GestureType.SWIPE_LEFT)
                } else {
                    if (dy < 0) {
                        // swipe up: open drawer directly unless remapped to something else
                        if (gesturePreference.getAction(GestureType.SWIPE_UP) == GestureAction.APP_DRAWER) {
                            openDrawer()
                        } else {
                            runGestureAction(GestureType.SWIPE_UP)
                        }
                    } else {
                        if (drawerOpen) closeDrawer() else runGestureAction(GestureType.SWIPE_DOWN)
                    }
                }
                return true
            }
        })

        binding.root.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun runGestureAction(type: GestureType) {
        val action = gesturePreference.getAction(type)
        val shouldOpenDrawer = gestureExecutor.execute(action, gesturePreference.getLaunchPackage(type))
        if (shouldOpenDrawer) openDrawer()
    }

    // --- Long-press: Wallpapers only (Guest) / Wallpapers + Home settings (Owner) ---

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
                    startActivity(Intent(this, WallpaperPickerActivity::class.java))
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
        private const val PINCH_IN_THRESHOLD = 0.85f
        private const val SWIPE_MIN_DISTANCE = 100f
        private const val MENU_WALLPAPERS = 1
        private const val MENU_HOME_SETTINGS = 2
    }
}
