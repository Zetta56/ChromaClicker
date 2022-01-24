package com.example.csac.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import com.example.csac.AutoClickService
import com.example.csac.R

// To add another view, just add it with a new layoutParams and call windowManager.addView()
class OverlayService : Service() {

    private lateinit var overlayMenu: OverlayMenu
    private lateinit var autoClickIntent: Intent
    private lateinit var windowManager: WindowManager
    private var circles = mutableListOf<CircleView>()

    companion object {
        fun createOverlayLayout(width: Int, height: Int, gravity: Int = Gravity.NO_GRAVITY,
                focusable: Boolean = false): WindowManager.LayoutParams {
            val layoutParams = WindowManager.LayoutParams()
            // Convert width and height from pixels to dp
            layoutParams.width = (width * Resources.getSystem().displayMetrics.density).toInt()
            layoutParams.height = (height * Resources.getSystem().displayMetrics.density).toInt()
            // Display this on top of other applications
            layoutParams.type = if(Build.VERSION.SDK_INT >= 26) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("Deprecation")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            // Don't grab input focus
            if(!focusable) {
                layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            }
            // Make the underlying application visible through any transparent sections
            layoutParams.format = PixelFormat.TRANSLUCENT
            // Position layout using gravity
            layoutParams.gravity = gravity
            return layoutParams
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        autoClickIntent = Intent(applicationContext, AutoClickService::class.java)
        overlayMenu = OverlayMenu(applicationContext, windowManager, circles, autoClickIntent)
        makeNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    // Don't bind this service to anything
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    // Destroy created views when this service is stopped
    override fun onDestroy() {
        circles.forEach { circle -> windowManager.removeView(circle) }
        overlayMenu.onDestroy()
        autoClickIntent.putExtra("enabled", false)
        applicationContext.startService(autoClickIntent)
        super.onDestroy()
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