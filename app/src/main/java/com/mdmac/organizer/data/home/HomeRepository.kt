package com.mdmac.organizer.data.home

import android.content.Context
import com.mdmac.organizer.data.apps.AppsRepository
import com.mdmac.organizer.data.apps.InstalledApp
import com.mdmac.organizer.profile.Profile

class HomeRepository(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val appsRepository = AppsRepository(appContext)

    fun getColumns(profile: Profile): Int =
        if (profile == Profile.GUEST) GUEST_COLUMNS else prefs.getInt(KEY_OWNER_COLUMNS, DEFAULT_OWNER_COLUMNS)

    fun getRows(profile: Profile): Int =
        if (profile == Profile.GUEST) GUEST_ROWS else prefs.getInt(KEY_OWNER_ROWS, DEFAULT_OWNER_ROWS)

    fun getDockSlots(profile: Profile): Int =
        if (profile == Profile.GUEST) GUEST_DOCK_SLOTS else prefs.getInt(KEY_OWNER_DOCK_SLOTS, DEFAULT_OWNER_DOCK_SLOTS)

    fun setOwnerColumns(value: Int): Boolean {
        val clamped = value.coerceIn(OWNER_COLUMNS_MIN, OWNER_COLUMNS_MAX)
        val occupiedColumns = minColumnsToFit(getHomePackages(Profile.OWNER).size, getRows(Profile.OWNER))
        if (clamped < occupiedColumns) return false
        prefs.edit().putInt(KEY_OWNER_COLUMNS, clamped).apply()
        return true
    }

    fun setOwnerRows(value: Int): Boolean {
        val clamped = value.coerceIn(OWNER_ROWS_MIN, OWNER_ROWS_MAX)
        val occupiedRows = minRowsToFit(getHomePackages(Profile.OWNER).size, getColumns(Profile.OWNER))
        if (clamped < occupiedRows) return false
        prefs.edit().putInt(KEY_OWNER_ROWS, clamped).apply()
        return true
    }

    fun setOwnerDockSlots(value: Int): Boolean {
        val clamped = value.coerceIn(OWNER_DOCK_MIN, OWNER_DOCK_MAX)
        if (appsRepository.getPinnedPackages().size > clamped) return false
        prefs.edit().putInt(KEY_OWNER_DOCK_SLOTS, clamped).apply()
        appsRepository.setDockSlotCount(clamped)
        return true
    }

    private fun minColumnsToFit(appCount: Int, rows: Int): Int =
        if (rows <= 0) OWNER_COLUMNS_MIN else ((appCount + rows - 1) / rows).coerceAtLeast(OWNER_COLUMNS_MIN)

    private fun minRowsToFit(appCount: Int, columns: Int): Int =
        if (columns <= 0) OWNER_ROWS_MIN else ((appCount + columns - 1) / columns).coerceAtLeast(OWNER_ROWS_MIN)

    // --- Curated home-grid apps ---

    fun getHomePackages(profile: Profile): List<String> {
        val key = if (profile == Profile.GUEST) KEY_GUEST_APPS else KEY_OWNER_APPS
        val stored = prefs.getStringSet(key, emptySet()) ?: emptySet()
        return stored.sorted()
    }

    fun setHomePackages(profile: Profile, packages: Set<String>) {
        val key = if (profile == Profile.GUEST) KEY_GUEST_APPS else KEY_OWNER_APPS
        prefs.edit().putStringSet(key, packages).apply()
    }

    suspend fun getHomeApps(profile: Profile): List<InstalledApp> {
        val all = appsRepository.getLaunchableApps()
        val wanted = getHomePackages(profile).toSet()
        return all.filter { it.packageName in wanted }
    }

    // --- Guest's drawer visibility: a distinct list from Guest's home apps ---

    fun getGuestDrawerPackages(): Set<String> =
        prefs.getStringSet(KEY_GUEST_DRAWER_APPS, emptySet()) ?: emptySet()

    fun setGuestDrawerPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_GUEST_DRAWER_APPS, packages).apply()
    }

    companion object {
        private const val PREFS_NAME = "home_prefs"
        private const val KEY_OWNER_COLUMNS = "owner_columns"
        private const val KEY_OWNER_ROWS = "owner_rows"
        private const val KEY_OWNER_DOCK_SLOTS = "owner_dock_slots"
        private const val KEY_GUEST_APPS = "guest_home_apps"
        private const val KEY_OWNER_APPS = "owner_home_apps"
        private const val KEY_GUEST_DRAWER_APPS = "guest_drawer_apps"

        const val GUEST_COLUMNS = 5
        const val GUEST_ROWS = 5
        const val GUEST_DOCK_SLOTS = 5

        const val DEFAULT_OWNER_COLUMNS = 5
        const val DEFAULT_OWNER_ROWS = 6
        const val DEFAULT_OWNER_DOCK_SLOTS = 5
        const val OWNER_COLUMNS_MIN = 3
        const val OWNER_COLUMNS_MAX = 6
        const val OWNER_ROWS_MIN = 3
        const val OWNER_ROWS_MAX = 8
        const val OWNER_DOCK_MIN = 4
        const val OWNER_DOCK_MAX = 6
    }
}
