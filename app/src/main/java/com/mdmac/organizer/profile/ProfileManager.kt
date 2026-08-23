package com.mdmac.organizer.profile

import android.content.Context

enum class Profile { GUEST, OWNER }

/** Tracks which profile (Guest/Owner) is currently active on the Home screen. */
class ProfileManager(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getCurrentProfile(): Profile {
        val stored = prefs.getString(KEY_PROFILE, Profile.GUEST.name)
        return runCatching { Profile.valueOf(stored ?: Profile.GUEST.name) }
            .getOrDefault(Profile.GUEST)
    }

    fun setCurrentProfile(profile: Profile) {
        prefs.edit().putString(KEY_PROFILE, profile.name).apply()
    }

    fun toggle() {
        setCurrentProfile(if (getCurrentProfile() == Profile.GUEST) Profile.OWNER else Profile.GUEST)
    }

    companion object {
        private const val PREFS_NAME = "profile_prefs"
        private const val KEY_PROFILE = "current_profile"
    }
}
