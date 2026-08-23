package com.mdmac.organizer.ui.settings

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.mdmac.organizer.R
import com.mdmac.organizer.databinding.ActivitySettingsBinding
import com.mdmac.organizer.databinding.ItemSettingsRowBinding
import com.mdmac.organizer.theme.BaseActivity
import com.mdmac.organizer.theme.ThemeMode
import com.mdmac.organizer.theme.ThemePreference
import androidx.appcompat.app.AlertDialog

private data class SettingsRow(
    val title: String,
    val subtitleProvider: () -> String,
    val iconColorRes: Int,
    val onClick: () -> Unit
)

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var themePreference: ThemePreference
    private val rowViews = mutableListOf<Pair<SettingsRow, View>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        themePreference = ThemePreference(this)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.footerText.text = getString(R.string.settings_footer)

        buildRows()
        setupSearch()
    }

    override fun onResume() {
        super.onResume()
        refreshSubtitles()
    }

    private fun buildRows() {
        val rows = listOf(
            SettingsRow(
                getString(R.string.settings_row_default_launcher_title),
                { getString(R.string.settings_row_default_launcher_subtitle_stub) },
                R.color.row_icon_launcher
            ) { showComingSoon("Batch E") },
            SettingsRow(
                getString(R.string.settings_row_home_screen_title),
                { getString(R.string.settings_row_home_screen_subtitle) },
                R.color.row_icon_home
            ) { showComingSoon("Batch B") },
            SettingsRow(
                getString(R.string.settings_row_app_drawer_title),
                { getString(R.string.settings_row_app_drawer_subtitle) },
                R.color.row_icon_drawer
            ) { showComingSoon("Batch B") },
            SettingsRow(
                getString(R.string.settings_row_gestures_title),
                { getString(R.string.settings_row_gestures_subtitle) },
                R.color.row_icon_gestures
            ) { showComingSoon("Batch C") },
            SettingsRow(
                getString(R.string.settings_row_wallpaper_title),
                { getString(R.string.settings_row_wallpaper_subtitle) },
                R.color.row_icon_wallpaper
            ) { showComingSoon("Batch C") },
            SettingsRow(
                getString(R.string.settings_row_theme_title),
                { themeSubtitle() },
                R.color.row_icon_theme
            ) { showThemeDialog() },
            SettingsRow(
                getString(R.string.settings_row_apps_title),
                { getString(R.string.settings_row_apps_subtitle) },
                R.color.row_icon_apps
            ) { showComingSoon("a later batch") },
            SettingsRow(
                getString(R.string.settings_row_touch_block_title),
                { getString(R.string.settings_row_touch_block_subtitle_stub) },
                R.color.row_icon_touchblock
            ) { showComingSoon("Batch D") },
            SettingsRow(
                getString(R.string.settings_row_general_title),
                { getString(R.string.settings_row_general_subtitle) },
                R.color.row_icon_general
            ) { startActivity(Intent(this, LegacySettingsActivity::class.java)) }
        )

        rows.forEach { row ->
            val rowBinding = ItemSettingsRowBinding.inflate(
                LayoutInflater.from(this), binding.rowContainer, false
            )
            rowBinding.rowIcon.backgroundTintList =
                androidx.core.content.ContextCompat.getColorStateList(this, row.iconColorRes)
            rowBinding.rowTitle.text = row.title
            rowBinding.rowSubtitle.text = row.subtitleProvider()
            rowBinding.root.setOnClickListener { row.onClick() }
            binding.rowContainer.addView(rowBinding.root)
            rowViews.add(row to rowBinding.root)
        }
    }

    private fun refreshSubtitles() {
        rowViews.forEach { (row, view) ->
            val subtitleView = view.findViewById<android.widget.TextView>(R.id.rowSubtitle)
            subtitleView.text = row.subtitleProvider()
        }
    }

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim()?.lowercase().orEmpty()
                rowViews.forEach { (row, view) ->
                    view.visibility = if (query.isEmpty() || row.title.lowercase().contains(query)) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun themeSubtitle(): String = when (themePreference.getMode()) {
        ThemeMode.LIGHT -> getString(R.string.theme_light)
        ThemeMode.DARK -> getString(R.string.theme_dark)
        ThemeMode.LIGHT_GRAY -> getString(R.string.theme_light_gray)
    }

    private fun showThemeDialog() {
        val options = arrayOf(
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_light_gray)
        )
        val modes = arrayOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.LIGHT_GRAY)
        val currentIndex = modes.indexOf(themePreference.getMode())

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_row_theme_title)
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                themePreference.setMode(modes[which])
                dialog.dismiss()
                recreate()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showComingSoon(batch: String) {
        Toast.makeText(this, "This section is built in $batch — not wired up yet", Toast.LENGTH_SHORT).show()
    }
}
