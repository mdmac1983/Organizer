package com.mdmac.organizer.gestures

import android.content.Context

/** Stores which action each gesture is mapped to, plus a target package for LAUNCH_APP. */
class GesturePreference(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAction(type: GestureType): GestureAction {
        val stored = prefs.getString(actionKey(type), defaultFor(type).name)
        return runCatching { GestureAction.valueOf(stored ?: defaultFor(type).name) }
            .getOrDefault(defaultFor(type))
    }

    fun setAction(type: GestureType, action: GestureAction) {
        prefs.edit().putString(actionKey(type), action.name).apply()
        if (action != GestureAction.LAUNCH_APP) {
            prefs.edit().remove(packageKey(type)).apply()
        }
    }

    fun getLaunchPackage(type: GestureType): String? = prefs.getString(packageKey(type), null)

    fun setLaunchPackage(type: GestureType, packageName: String) {
        prefs.edit().putString(packageKey(type), packageName).apply()
    }

    private fun defaultFor(type: GestureType): GestureAction = when (type) {
        GestureType.SWIPE_UP -> GestureAction.APP_DRAWER
        else -> GestureAction.NONE
    }

    private fun actionKey(type: GestureType) = "action_${type.name}"
    private fun packageKey(type: GestureType) = "package_${type.name}"

    companion object {
        private const val PREFS_NAME = "gesture_prefs"
    }
}
