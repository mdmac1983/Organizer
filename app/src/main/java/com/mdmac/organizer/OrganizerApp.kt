package com.mdmac.organizer

import android.app.Application

class OrganizerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Global init hooks (database, etc.) will go here as we add tabs
    }
}
