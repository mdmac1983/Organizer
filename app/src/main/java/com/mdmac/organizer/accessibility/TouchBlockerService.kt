package com.mdmac.organizer.accessibility

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.mdmac.organizer.R
import kotlin.math.abs

class TouchBlockerService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    private val handler = Handler(Looper.getMainLooper())
    private var longPressRunnable: Runnable? = null
    private var downX = 0f
    private var downY = 0f

    private val disableReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            setBlocking(false)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        ContextCompat.registerReceiver(
            this,
            disableReceiver,
            IntentFilter(ACTION_DISABLE_BLOCKING),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No event handling needed — this service exists only to obtain the
        // accessibility-overlay window privilege used by the touch blocker.
    }

    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        setBlocking(false)
        instance = null
        runCatching { unregisterReceiver(disableReceiver) }
        return super.onUnbind(intent)
    }

    fun isBlocking(): Boolean = overlayView != null

    fun setBlocking(enabled: Boolean) {
        if (enabled) showOverlay() else hideOverlay()
    }

    private fun showOverlay() {
        if (overlayView != null) return
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager = wm

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        val view = FrameLayout(this).apply {
            setBackgroundColor(OVERLAY_TINT)
            setOnTouchListener { _, event -> handleOverlayTouch(event); true }
        }
        wm.addView(view, params)
        overlayView = view
        showPersistentNotification()
    }

    private fun hideOverlay() {
        val view = overlayView ?: return
        windowManager?.removeView(view)
        overlayView = null
        cancelPersistentNotification()
        longPressRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun handleOverlayTouch(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                val runnable = Runnable { setBlocking(false) }
                longPressRunnable = runnable
                handler.postDelayed(runnable, DISABLE_HOLD_MS)
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(event.x - downX) > TOUCH_SLOP_PX || abs(event.y - downY) > TOUCH_SLOP_PX) {
                    longPressRunnable?.let { handler.removeCallbacks(it) }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                longPressRunnable?.let { handler.removeCallbacks(it) }
            }
        }
    }

    private fun showPersistentNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Touch Blocker", NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(channel)
        }
        val disableIntent = Intent(ACTION_DISABLE_BLOCKING).setPackage(packageName)
        val disablePendingIntent = PendingIntent.getBroadcast(
            this, 0, disableIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentTitle(getString(R.string.touch_blocker_notification_title))
            .setContentText(getString(R.string.touch_blocker_notification_text))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, getString(R.string.touch_blocker_disable_action), disablePendingIntent)
            .build()
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun cancelPersistentNotification() {
        getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
    }

    companion object {
        var instance: TouchBlockerService? = null
            private set

        private const val ACTION_DISABLE_BLOCKING =
            "com.mdmac.organizer.action.DISABLE_TOUCH_BLOCKER"
        private const val CHANNEL_ID = "touch_blocker_channel"
        private const val NOTIFICATION_ID = 4201
        private const val DISABLE_HOLD_MS = 2000L
        private const val TOUCH_SLOP_PX = 40f
        private const val OVERLAY_TINT = 0x33008080 // faint teal, signals the blocker is active
    }
}
