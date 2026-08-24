package com.mdmac.organizer.data.apps

import android.content.Context

/** Per-app custom label and custom icon (picked from gallery), keyed by package name. */
class AppCustomizationPreference(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getCustomLabel(packageName: String): String? = prefs.getString(labelKey(packageName), null)

    fun setCustomLabel(packageName: String, label: String?) {
        if (label.isNullOrBlank()) {
            prefs.edit().remove(labelKey(packageName)).apply()
        } else {
            prefs.edit().putString(labelKey(packageName), label).apply()
        }
    }

    fun getCustomIconPath(packageName: String): String? = prefs.getString(iconKey(packageName), null)

    fun setCustomIconPath(packageName: String, path: String?) {
        if (path == null) {
            prefs.edit().remove(iconKey(packageName)).apply()
        } else {
            prefs.edit().putString(iconKey(packageName), path).apply()
        }
    }

    private fun labelKey(packageName: String) = "label_$packageName"
    private fun iconKey(packageName: String) = "icon_$packageName"

    companion object {
        private const val PREFS_NAME = "app_customization_prefs"
    }
}
