package com.mdmac.organizer

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class OrganizerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Our theme system is fully custom (Light/Dark/Light-gray, chosen in-app) —
        // AppCompatDelegate's default "follow system" behavior needs to be explicitly
        // turned off, or it silently overrides parts of our theme whenever the
        // device's own dark-mode setting is on, regardless of which theme we've set.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
