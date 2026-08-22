package com.mdmac.organizer.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class PlannerDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(
            context,
            "Device admin enabled — screen-lock enforcement and uninstall protection are active",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "Device admin disabled", Toast.LENGTH_SHORT).show()
    }
}
