package com.mdmac.organizer.gestures

enum class GestureAction(val label: String) {
    NONE("None"),
    APP_DRAWER("Open app drawer"),
    NOTIFICATIONS("Expand notifications"),
    QUICK_SETTINGS("Expand quick settings"),
    RECENT_APPS("Recent apps"),
    SCREEN_LOCK("Lock screen"),
    LAUNCH_APP("Launch app"),
    SWITCH_LAUNCHER("Switch default launcher")
}

enum class GestureType(val label: String) {
    SWIPE_UP("Swipe up"),
    SWIPE_DOWN("Swipe down"),
    SWIPE_LEFT("Swipe left"),
    SWIPE_RIGHT("Swipe right"),
    DOUBLE_TAP("Double tap"),
    PINCH_IN("Pinch in")
}
