package com.example.csac

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton

class OverlayService : Service() {
    private val buttonIds = arrayOf(R.id.playButton, R.id.plusButton, R.id.minusButton)
    lateinit var view: View
    lateinit var windowManager: WindowManager
    lateinit var layoutParams: WindowManager.LayoutParams

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        val typeParam = if(Build.VERSION.SDK_INT >= 26) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        layoutParams = WindowManager.LayoutParams(
            // Convert dimensions from pixels to dp
            (55 * applicationContext.resources.displayMetrics.density).toInt(),
            (165 * applicationContext.resources.displayMetrics.density).toInt(),
            // Display this on top of other application windows
            typeParam,
            // Don't grab input focus
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            // Make the underlying application window visible through any transparent sections
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.START
        view = View.inflate(applicationContext, R.layout.overlay, null)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(view, layoutParams)

        // Add event listeners
        val listener = OverlayListener(this)
        for(buttonId in buttonIds) {
            view.findViewById<ImageButton>(buttonId).setOnClickListener(listener)
            view.findViewById<ImageButton>(buttonId).setOnTouchListener(listener)
        }
        view.setOnTouchListener(listener)
        makeNotification()
    }

    // Don't bind this service to anything
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    // Destroy the created view when this service is stopped
    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(view)
    }

    private fun makeNotification() {
        if(Build.VERSION.SDK_INT >= 26) {
            // Create notification channel for foreground service
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "csac_overlay",
                getString(R.string.overlay_notification),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setSound(null, null)
            notificationManager.createNotificationChannel(channel)

            // Send notification
            val builder = Notification.Builder(this, "csac_overlay")
            startForeground(1, builder.build())
        }
    }
}