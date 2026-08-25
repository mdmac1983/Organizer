package com.mdmac.organizer.theme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

/**
 * All activities extend this instead of AppCompatActivity directly, so the
 * user's Follow-system/Material-Gray choice applies consistently everywhere,
 * including status bar and nav bar icon color, which Android otherwise
 * defaults to following the device's own system light/dark setting rather
 * than our app's resolved theme.
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val themePreference = ThemePreference(this)
        setTheme(themePreference.resolveStyleRes())
        super.onCreate(savedInstanceState)

        val isLightBackground = when (themePreference.getMode()) {
            ThemeMode.SYSTEM -> !themePreference.isSystemInDarkMode()
            ThemeMode.MATERIAL_GRAY -> true
        }
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = isLightBackground
        insetsController.isAppearanceLightNavigationBars = isLightBackground
    }
}
