package com.mdmac.organizer.data.apps

import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val isPinned: Boolean = false,
    val isHidden: Boolean = false
)
