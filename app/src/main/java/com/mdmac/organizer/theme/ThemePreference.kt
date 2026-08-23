package com.mdmac.organizer.theme

import android.content.Context
import androidx.annotation.StyleRes
import com.mdmac.organizer.R

enum class ThemeMode { LIGHT, DARK, LIGHT_GRAY }

/**
 * Stores the user's theme choice (Light / Dark / Light gray) and resolves it
 * to a concrete style resource. Applied explicitly via setTheme() in
 * BaseActivity.onCreate() — independent of the device's system dark-mode
 * setting, since "System" is no longer one of the options.
 */
class ThemePreference(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getMode(): ThemeMode {
        val stored = prefs.getString(KEY_MODE, ThemeMode.LIGHT.name)
        return runCatching { ThemeMode.valueOf(stored ?: ThemeMode.LIGHT.name) }
            .getOrDefault(ThemeMode.LIGHT)
    }

    fun setMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
    }

    @StyleRes
    fun resolveStyleRes(): Int = when (getMode()) {
        ThemeMode.LIGHT -> R.style.Theme_Organizer_Light
        ThemeMode.DARK -> R.style.Theme_Organizer_Dark
        ThemeMode.LIGHT_GRAY -> R.style.Theme_Organizer_LightGray
    }

    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_MODE = "theme_mode"
    }
}
