package com.mdmac.organizer.gestures

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.mdmac.organizer.accessibility.TouchBlockerService

class GestureExecutor(private val context: Context) {

    /** Returns true if the drawer should be shown (caller handles that; everything else is executed here). */
    fun execute(action: GestureAction, launchPackage: String?): Boolean {
        when (action) {
            GestureAction.NONE -> {}
            GestureAction.APP_DRAWER -> return true
            GestureAction.NOTIFICATIONS -> runGlobal(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
            GestureAction.QUICK_SETTINGS -> runGlobal(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
            GestureAction.RECENT_APPS -> runGlobal(AccessibilityService.GLOBAL_ACTION_RECENTS)
            GestureAction.SCREEN_LOCK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    runGlobal(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
                } else {
                    Toast.makeText(context, "Screen lock gesture needs Android 9+", Toast.LENGTH_SHORT).show()
                }
            }
            GestureAction.LAUNCH_APP -> {
                launchPackage?.let {
                    context.packageManager.getLaunchIntentForPackage(it)?.let { intent ->
                        context.startActivity(intent)
                    }
                }
            }
            GestureAction.SWITCH_LAUNCHER -> {
                context.startActivity(
                    Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
        return false
    }

    private fun runGlobal(action: Int) {
        val ok = TouchBlockerService.runGlobalAction(action)
        if (!ok) {
            Toast.makeText(context, "Enable the accessibility service in Settings for this gesture", Toast.LENGTH_LONG).show()
        }
    }
}
