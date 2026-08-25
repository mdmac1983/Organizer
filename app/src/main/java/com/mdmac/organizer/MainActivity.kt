package com.mdmac.organizer

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.mdmac.organizer.data.apps.AppsRepository
import com.mdmac.organizer.data.apps.InstalledApp
import com.mdmac.organizer.data.wallpaper.WallpaperRepository
import com.mdmac.organizer.databinding.ActivityMainBinding
import com.mdmac.organizer.theme.BaseActivity
import com.mdmac.organizer.ui.OrganizerPagerAdapter
import com.mdmac.organizer.ui.apps.AppGridAdapter
import com.mdmac.organizer.ui.apps.AppGridItem
import com.mdmac.organizer.ui.home.HomeActivity
import com.mdmac.organizer.ui.settings.SettingsActivity
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var searchAdapter: AppGridAdapter
    private var allInstalledApps: List<InstalledApp> = emptyList()
    private var footerBaseBottomPaddingPx = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        footerBaseBottomPaddingPx = binding.footerText.paddingBottom

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.paddingBottom)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.footerText) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, footerBaseBottomPaddingPx + bars.bottom)
            insets
        }

        binding.viewPager.adapter = OrganizerPagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 3

        val tabTitles = listOf(
            getString(R.string.tab_planner),
            getString(R.string.tab_contacts),
            getString(R.string.tab_passwords),
            getString(R.string.tab_notes)
        )

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        val startTab = intent.getIntExtra(EXTRA_START_TAB, 0)
        binding.viewPager.setCurrentItem(startTab, false)

        binding.footerText.text = getString(R.string.settings_footer)
        loadTabBackground()
        setupAppSearch()

        binding.btnBottomSearch.setOnClickListener { toggleAppSearch() }
        binding.btnBottomHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        binding.btnBottomSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadTabBackground()
    }

    private fun loadTabBackground() {
        val wallpaperRepository = WallpaperRepository(this)
        val customPath = wallpaperRepository.getCustomUri()
        val bundledRes = wallpaperRepository.getBundledResId()
        when {
            customPath != null && File(customPath).exists() -> {
                binding.tabBackgroundImageView.setImageBitmap(BitmapFactory.decodeFile(customPath))
            }
            bundledRes != null -> binding.tabBackgroundImageView.setImageResource(bundledRes)
            else -> binding.tabBackgroundImageView.setImageResource(R.drawable.tab_background)
        }
    }

    private fun setupAppSearch() {
        searchAdapter = AppGridAdapter(
            onAppClick = { app ->
                packageManager.getLaunchIntentForPackage(app.packageName)?.let { startActivity(it) }
            },
            onAppLongClick = { _, _ -> false },
            onHiddenFolderClick = {}
        )
        binding.appSearchResultsRecyclerView.layoutManager = GridLayoutManager(this, 5)
        binding.appSearchResultsRecyclerView.adapter = searchAdapter

        lifecycleScope.launch {
            allInstalledApps = AppsRepository(this@MainActivity).getLaunchableApps()
        }

        binding.appSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim().orEmpty()
                if (query.isEmpty()) {
                    binding.appSearchResultsRecyclerView.visibility = View.GONE
                } else {
                    binding.appSearchResultsRecyclerView.visibility = View.VISIBLE
                    val filtered = allInstalledApps.filter { it.label.contains(query, ignoreCase = true) }
                    searchAdapter.submitList(filtered.map { AppGridItem.AppEntry(it) })
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun toggleAppSearch() {
        val showing = binding.appSearchInput.visibility == View.VISIBLE
        if (showing) {
            binding.appSearchInput.visibility = View.GONE
            binding.appSearchInput.setText("")
            binding.appSearchResultsRecyclerView.visibility = View.GONE
        } else {
            binding.appSearchInput.visibility = View.VISIBLE
            binding.appSearchInput.requestFocus()
        }
    }

    companion object {
        const val EXTRA_START_TAB = "start_tab"
    }
}
