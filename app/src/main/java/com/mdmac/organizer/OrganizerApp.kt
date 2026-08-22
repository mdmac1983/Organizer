package com.mdmac.organizer

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors

class OrganizerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val prefs = getSharedPreferences("organizer_settings", MODE_PRIVATE)

        val darkTheme = prefs.getBoolean("dark_theme", false)
        AppCompatDelegate.setDefaultNightMode(
            if (darkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        val dynamicColor = prefs.getBoolean("dynamic_color", true)
        if (dynamicColor) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
}
