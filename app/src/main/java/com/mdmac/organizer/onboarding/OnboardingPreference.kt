package com.mdmac.organizer.onboarding

import android.content.Context

class OnboardingPreference(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isComplete(): Boolean = prefs.getBoolean(KEY_COMPLETE, false)

    fun setComplete() {
        prefs.edit().putBoolean(KEY_COMPLETE, true).apply()
    }

    companion object {
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_COMPLETE = "onboarding_complete"
    }
}
