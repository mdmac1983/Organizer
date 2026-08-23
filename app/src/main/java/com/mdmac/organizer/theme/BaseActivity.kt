package com.mdmac.organizer.theme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * All activities extend this instead of AppCompatActivity directly, so the
 * user's Light/Dark/Light-gray choice applies consistently everywhere, not
 * just on the Settings screen.
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemePreference(this).resolveStyleRes())
        super.onCreate(savedInstanceState)
    }
}
