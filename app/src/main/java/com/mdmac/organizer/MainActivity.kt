package com.mdmac.organizer

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mdmac.organizer.data.wallpaper.WallpaperRepository
import com.mdmac.organizer.databinding.ActivityMainBinding
import com.mdmac.organizer.theme.BaseActivity
import com.mdmac.organizer.ui.OrganizerPagerAdapter
import com.mdmac.organizer.ui.pinned.PinnedActivity
import com.mdmac.organizer.ui.settings.SettingsActivity
import java.io.File

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_pinned -> {
                    startActivity(Intent(this, PinnedActivity::class.java))
                    true
                }
                R.id.action_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
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

    companion object {
        const val EXTRA_START_TAB = "start_tab"
    }
}
