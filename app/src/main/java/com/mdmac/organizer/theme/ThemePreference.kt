package com.mdmac.organizer.theme

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.annotation.StyleRes
import com.mdmac.organizer.R

enum class ThemeMode { SYSTEM, MATERIAL_GRAY }

/**
 * "System" dynamically renders Light or Dark based on the device's own
 * dark-mode setting — checked via Resources.getSystem(), the static
 * system-wide resources instance, since it's completely independent of
 * our app's own forced AppCompatDelegate.MODE_NIGHT_NO override (needed
 * elsewhere to keep Material Gray fully decoupled from system). Querying
 * through UiModeManager or this app's own Context.resources would just
 * read that override back to itself instead of the real device setting.
 * "Material Gray" is a fixed, manual-only third option.
 */
class ThemePreference(private val context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getMode(): ThemeMode {
        val stored = prefs.getString(KEY_MODE, ThemeMode.SYSTEM.name)
        return runCatching { ThemeMode.valueOf(stored ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    fun setMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
    }

    fun isSystemInDarkMode(): Boolean {
        val uiMode = Resources.getSystem().configuration.uiMode
        return (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    @StyleRes
    fun resolveStyleRes(): Int = when (getMode()) {
        ThemeMode.SYSTEM -> if (isSystemInDarkMode()) R.style.Theme_Organizer_Dark else R.style.Theme_Organizer_Light
        ThemeMode.MATERIAL_GRAY -> R.style.Theme_Organizer_LightGray
    }

    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_MODE = "theme_mode"
    }
}
