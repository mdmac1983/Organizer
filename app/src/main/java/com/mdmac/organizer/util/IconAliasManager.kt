package com.mdmac.organizer.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper

object IconAliasManager {

    enum class Alias(val suffix: String, val displayLabel: String) {
        DEFAULT(".LauncherDefault", "Simple Planner"),
        OWNER_INFO(".LauncherOwnerInfo", "Owner Info"),
        CALCULATOR(".LauncherCalculator", "Calculator"),
        MUSIC(".LauncherMusic", "Music")
    }

    fun setActive(context: Context, alias: Alias) {
        Handler(Looper.getMainLooper()).postDelayed({
            val pm = context.packageManager
            Alias.values().forEach { candidate ->
                val state = if (candidate == alias) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                pm.setComponentEnabledSetting(
                    ComponentName(context.packageName, context.packageName + candidate.suffix),
                    state,
                    PackageManager.DONT_KILL_APP
                )
            }
        }, 1000)
    }
}
