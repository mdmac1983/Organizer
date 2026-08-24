package com.mdmac.organizer.ui.onboarding

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.mdmac.organizer.MainActivity
import com.mdmac.organizer.R
import com.mdmac.organizer.data.apps.AppsRepository
import com.mdmac.organizer.data.apps.InstalledApp
import com.mdmac.organizer.data.home.HomeRepository
import com.mdmac.organizer.data.wallpaper.WallpaperRepository
import com.mdmac.organizer.databinding.ActivityLauncherSetupBinding
import com.mdmac.organizer.databinding.ItemAppChecklistBinding
import com.mdmac.organizer.onboarding.OnboardingPreference
import com.mdmac.organizer.profile.Profile
import com.mdmac.organizer.theme.BaseActivity
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class LauncherSetupActivity : BaseActivity() {

    private lateinit var binding: ActivityLauncherSetupBinding
    private lateinit var appsRepository: AppsRepository
    private lateinit var homeRepository: HomeRepository
    private lateinit var wallpaperRepository: WallpaperRepository
    private lateinit var onboardingPreference: OnboardingPreference

    private var allApps: List<InstalledApp> = emptyList()
    private var currentStep = 0
    private val steps by lazy {
        listOf(binding.stepLauncher, binding.stepOwnerHome, binding.stepGuestHome, binding.stepGuestDrawer, binding.stepWallpaper)
    }

    private val bundledWallpapers = listOf(
        R.drawable.wallpaper_bubblegum, R.drawable.wallpaper_flora, R.drawable.wallpaper_canyon,
        R.drawable.wallpaper_escape, R.drawable.wallpaper_kepler, R.drawable.wallpaper_outofthebox,
        R.drawable.wallpaper_work, R.drawable.wallpaper_chroma, R.drawable.wallpaper_architecture
    )

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) saveCustomWallpaper(uri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appsRepository = AppsRepository(this)
        homeRepository = HomeRepository(this)
        wallpaperRepository = WallpaperRepository(this)
        onboardingPreference = OnboardingPreference(this)

        binding.btnBack.setOnClickListener { goToStep(currentStep - 1) }
        binding.btnNext.setOnClickListener {
            if (currentStep == steps.lastIndex) finishSetup() else goToStep(currentStep + 1)
        }
        binding.btnSetDefaultLauncher.setOnClickListener {
            startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        }

        lifecycleScope.launch {
            allApps = appsRepository.getLaunchableApps()
            buildChecklist(binding.ownerHomeChecklist, Profile.OWNER, isDrawer = false)
            buildChecklist(binding.guestHomeChecklist, Profile.GUEST, isDrawer = false)
            buildChecklist(binding.guestDrawerChecklist, Profile.GUEST, isDrawer = true)
        }
        buildWallpaperGrid()

        goToStep(0)
    }

    override fun onResume() {
        super.onResume()
        refreshDefaultLauncherStatus()
    }

    private fun goToStep(index: Int) {
        currentStep = index.coerceIn(0, steps.lastIndex)
        steps.forEachIndexed { i, view -> view.visibility = if (i == currentStep) View.VISIBLE else View.GONE }
        binding.btnBack.visibility = if (currentStep == 0) View.INVISIBLE else View.VISIBLE
        binding.btnNext.text = if (currentStep == steps.lastIndex) "Finish" else "Next"
        binding.stepIndicator.text = "Step ${currentStep + 1} of ${steps.size}"
        if (currentStep == 0) refreshDefaultLauncherStatus()
    }

    private fun refreshDefaultLauncherStatus() {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = packageManager.resolveActivity(homeIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
        val isDefault = resolved?.activityInfo?.packageName == packageName
        binding.launcherStatus.text = if (isDefault) "Currently your Home app" else "Not set yet — tap below"
    }

    private fun buildChecklist(container: android.widget.LinearLayout, profile: Profile, isDrawer: Boolean) {
        container.removeAllViews()
        val selected = (if (isDrawer) homeRepository.getGuestDrawerPackages() else homeRepository.getHomePackages(profile).toSet()).toMutableSet()

        allApps.forEach { app ->
            val itemBinding = ItemAppChecklistBinding.inflate(layoutInflater, container, false)
            itemBinding.appLabel.text = app.label
            itemBinding.appIcon.setImageDrawable(app.icon)
            itemBinding.checkbox.isChecked = app.packageName in selected
            itemBinding.checkbox.setOnCheckedChangeListener { _, checked ->
                if (checked) selected.add(app.packageName) else selected.remove(app.packageName)
                if (isDrawer) {
                    homeRepository.setGuestDrawerPackages(selected)
                } else {
                    homeRepository.setHomePackages(profile, selected)
                }
            }
            container.addView(itemBinding.root)
        }
    }

    private fun buildWallpaperGrid() {
        binding.wallpaperGrid.removeAllViews()
        bundledWallpapers.forEach { resId ->
            val thumb = android.widget.ImageView(this).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(96.dp(), 96.dp()).apply { setMargins(4.dp(), 4.dp(), 4.dp(), 4.dp()) }
                setImageResource(resId)
                scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                setOnClickListener { wallpaperRepository.setBundled(resId) }
            }
            binding.wallpaperGrid.addView(thumb)
        }
        val galleryTile = android.widget.ImageView(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(96.dp(), 96.dp()).apply { setMargins(4.dp(), 4.dp(), 4.dp(), 4.dp()) }
            setImageResource(android.R.drawable.ic_menu_gallery)
            scaleType = android.widget.ImageView.ScaleType.CENTER
            setBackgroundColor(resources.getColor(R.color.gray_200, theme))
            setOnClickListener { pickImageLauncher.launch("image/*") }
        }
        binding.wallpaperGrid.addView(galleryTile)
    }

    private fun saveCustomWallpaper(uri: Uri) {
        runCatching {
            val input = contentResolver.openInputStream(uri) ?: return
            val bitmap = BitmapFactory.decodeStream(input)
            input.close()
            val file = File(filesDir, "wallpaper_custom.jpg")
            FileOutputStream(file).use { out -> bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out) }
            wallpaperRepository.setCustomUri(file.absolutePath)
        }
    }

    private fun finishSetup() {
        onboardingPreference.setComplete()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()
}
