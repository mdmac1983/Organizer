package com.mdmac.organizer

import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mdmac.organizer.databinding.ActivityMainBinding
import com.mdmac.organizer.theme.BaseActivity
import com.mdmac.organizer.ui.OrganizerPagerAdapter

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
    }

    companion object {
        const val EXTRA_START_TAB = "start_tab"
    }
}
