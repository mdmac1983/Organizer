package com.mdmac.organizer

import android.app.Application
import com.mdmac.organizer.theme.ThemePreference

class OrganizerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemePreference(this).applyStoredMode()
    }
}
