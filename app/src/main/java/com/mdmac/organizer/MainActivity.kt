package com.mdmac.organizer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mdmac.organizer.databinding.ActivityMainBinding
import com.mdmac.organizer.ui.OrganizerPagerAdapter
import com.mdmac.organizer.ui.pinned.PinnedActivity
import com.mdmac.organizer.ui.settings.SettingsActivity
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

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
}
