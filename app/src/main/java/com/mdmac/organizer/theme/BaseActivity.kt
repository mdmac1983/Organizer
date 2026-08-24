package com.mdmac.organizer.theme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

/**
 * All activities extend this instead of AppCompatActivity directly, so the
 * user's Light/Dark/Light-gray choice applies consistently everywhere, not
 * just on the Settings screen — including status bar and nav bar icon color,
 * which Android otherwise defaults to following the device's own system
 * light/dark setting rather than our app's chosen theme.
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val themePreference = ThemePreference(this)
        setTheme(themePreference.resolveStyleRes())
        super.onCreate(savedInstanceState)

        val isLightBackground = themePreference.getMode() != ThemeMode.DARK
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = isLightBackground
        insetsController.isAppearanceLightNavigationBars = isLightBackground
    }
}
