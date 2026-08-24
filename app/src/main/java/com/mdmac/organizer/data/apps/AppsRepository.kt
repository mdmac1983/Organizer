package com.mdmac.organizer.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AppsRepository(context: Context) {

    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val customization = AppCustomizationPreference(appContext)

    /** Every launchable app on the device except this app itself, sorted by label. */
    suspend fun getLaunchableApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val pinned = getPinnedPackages()
        val hidden = getHiddenPackages()

        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = packageManager.queryIntentActivities(launcherIntent, 0)

        resolveInfos
            .asSequence()
            .filter { it.activityInfo.packageName != appContext.packageName }
            .distinctBy { it.activityInfo.packageName }
            .map { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val customIconPath = customization.getCustomIconPath(packageName)
                val icon: Drawable = if (customIconPath != null && File(customIconPath).exists()) {
                    Drawable.createFromPath(customIconPath) ?: resolveInfo.loadIcon(packageManager)
                } else {
                    resolveInfo.loadIcon(packageManager)
                }
                InstalledApp(
                    packageName = packageName,
                    label = customization.getCustomLabel(packageName) ?: resolveInfo.loadLabel(packageManager).toString(),
                    icon = icon,
                    isPinned = pinned.contains(packageName),
                    isHidden = hidden.contains(packageName)
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun getPinnedPackages(): Set<String> =
        prefs.getStringSet(KEY_PINNED, emptySet()) ?: emptySet()

    fun getHiddenPackages(): Set<String> =
        prefs.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()

    fun setPinned(packageName: String, pinned: Boolean) {
        val current = getPinnedPackages().toMutableSet()
        if (pinned) current.add(packageName) else current.remove(packageName)
        prefs.edit().putStringSet(KEY_PINNED, current).apply()
    }

    fun setHidden(packageName: String, hidden: Boolean) {
        val current = getHiddenPackages().toMutableSet()
        if (hidden) {
            current.add(packageName)
            setPinned(packageName, false)
        } else {
            current.remove(packageName)
        }
        prefs.edit().putStringSet(KEY_HIDDEN, current).apply()
    }

    fun getDockSlotCount(): Int = prefs.getInt(KEY_DOCK_SLOTS, DEFAULT_DOCK_SLOTS)

    fun setDockSlotCount(count: Int) {
        prefs.edit().putInt(KEY_DOCK_SLOTS, count.coerceIn(1, 7)).apply()
    }

    companion object {
        private const val PREFS_NAME = "apps_tab_prefs"
        private const val KEY_PINNED = "pinned_packages"
        private const val KEY_HIDDEN = "hidden_packages"
        private const val KEY_DOCK_SLOTS = "dock_slot_count"
        private const val DEFAULT_DOCK_SLOTS = 5
    }
}
