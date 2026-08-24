package com.mdmac.organizer.ui.home

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mdmac.organizer.R
import com.mdmac.organizer.accessibility.TouchBlockerService
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
    private val touchBlockHandler = Handler(Looper.getMainLooper())

    private lateinit var gridAdapter: AppGridAdapter
    private lateinit var dockAdapter: AppGridAdapter

    private var drawerOpen = false

    private var lastTapUpTime = 0L
    private var lastTapX = 0f
    private var lastTapY = 0f
    private var touchBlockHoldRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
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
            loadWallpaper()
            loadHome()
        } catch (t: Throwable) {
            showCrashScreen(t)
        }
    }

    private fun showCrashScreen(t: Throwable) {
        val scrollView = android.widget.ScrollView(this)
        val textView = android.widget.TextView(this).apply {
            setPadding(32, 32, 32, 32)
            textSize = 12f
            setTextColor(android.graphics.Color.BLACK)
            text = "HomeActivity crashed:\n\n" + android.util.Log.getStackTraceString(t)
        }
        scrollView.addView(textView)
        scrollView.setBackgroundColor(android.graphics.Color.WHITE)
        setContentView(scrollView)
    }

    override fun onResume() {
        super.onResume()
        try {
            loadWallpaper()
            loadHome()
        } catch (t: Throwable) {
            showCrashScreen(t)
        }
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

            override fun onLongPress(e: MotionEvent) {
                showHomeLongPressPopup(e.rawX.toInt(), e.rawY.toInt())
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

        val itemTouchListener = object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                handleTouchBlockGesture(e)
                scaleDetector.onTouchEvent(e)
                gestureDetector.onTouchEvent(e)
                return false
            }
        }
        binding.homeGridRecyclerView.addOnItemTouchListener(itemTouchListener)
        binding.homeDockRecyclerView.addOnItemTouchListener(itemTouchListener)

        binding.root.setOnTouchListener { _, event ->
            handleTouchBlockGesture(event)
            scaleDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun handleTouchBlockGesture(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val now = System.currentTimeMillis()
                val withinTimeout = (now - lastTapUpTime) in 0..DOUBLE_TAP_TIMEOUT_MS
                val withinSlop = abs(event.x - lastTapX) < TAP_SLOP_PX && abs(event.y - lastTapY) < TAP_SLOP_PX
                if (withinTimeout && withinSlop) {
                    val runnable = Runnable { toggleTouchBlocker() }
                    touchBlockHoldRunnable = runnable
                    touchBlockHandler.postDelayed(runnable, HOLD_THRESHOLD_MS)
                }
            }
            MotionEvent.ACTION_UP -> {
                touchBlockHoldRunnable?.let { touchBlockHandler.removeCallbacks(it) }
                touchBlockHoldRunnable = null
                lastTapUpTime = System.currentTimeMillis()
                lastTapX = event.x
                lastTapY = event.y
            }
            MotionEvent.ACTION_CANCEL -> {
                touchBlockHoldRunnable?.let { touchBlockHandler.removeCallbacks(it) }
                touchBlockHoldRunnable = null
            }
        }
    }

    private fun toggleTouchBlocker() {
        val service = TouchBlockerService.instance
        if (service == null) {
            Toast.makeText(this, "Enable the accessibility service in Settings first", Toast.LENGTH_LONG).show()
            return
        }
        service.setBlocking(!service.isBlocking())
    }

    private fun runGestureAction(type: GestureType) {
        val action = gesturePreference.getAction(type)
        val shouldOpenDrawer = gestureExecutor.execute(action, gesturePreference.getLaunchPackage(type))
        if (shouldOpenDrawer) openDrawer()
    }

    private fun showHomeLongPressPopup(touchX: Int, touchY: Int) {
        val popupView = layoutInflater.inflate(R.layout.popup_home_long_press, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        val isOwner = profileManager.getCurrentProfile() == Profile.OWNER
        popupView.findViewById<View>(R.id.rowHomeSettings).visibility =
            if (isOwner) View.VISIBLE else View.GONE

        popupView.findViewById<View>(R.id.rowWallpapers).setOnClickListener {
            popupWindow.dismiss()
            startActivity(Intent(this, WallpaperPickerActivity::class.java))
        }
        popupView.findViewById<View>(R.id.rowHomeSettings).setOnClickListener {
            popupWindow.dismiss()
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = popupView.measuredWidth
        val screenWidth = resources.displayMetrics.widthPixels
        val x = touchX.coerceIn(0, (screenWidth - popupWidth).coerceAtLeast(0))

        popupWindow.showAtLocation(binding.root, Gravity.NO_GRAVITY, x, touchY)
    }

    companion object {
        private const val PINCH_OUT_THRESHOLD = 1.15f
        private const val PINCH_IN_THRESHOLD = 0.85f
        private const val SWIPE_MIN_DISTANCE = 100f
        private const val DOUBLE_TAP_TIMEOUT_MS = 300L
        private const val TAP_SLOP_PX = 60f
        private const val HOLD_THRESHOLD_MS = 500L
    }
}
