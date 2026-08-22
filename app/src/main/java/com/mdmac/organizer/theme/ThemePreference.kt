package com.mdmac.organizer.theme

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

enum class ThemeMode { LIGHT, DARK, SYSTEM }

/**
 * Stores the user's theme choice and applies it via AppCompatDelegate's
 * night mode. This doesn't introduce a separate color palette — Light and
 * Dark both still resolve to the existing teal Theme.Organizer in
 * values/themes.xml and values-night/themes.xml respectively; this class
 * just controls which of the two applies regardless of the system setting.
 */
class ThemePreference(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getMode(): ThemeMode {
        val stored = prefs.getString(KEY_MODE, ThemeMode.SYSTEM.name)
        return runCatching { ThemeMode.valueOf(stored ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    fun setMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
        applyMode(mode)
    }

    /** Call once at app startup, before any Activity is created. */
    fun applyStoredMode() {
        applyMode(getMode())
    }

    private fun applyMode(mode: ThemeMode) {
        val nightMode = when (mode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_MODE = "theme_mode"
    }
}
