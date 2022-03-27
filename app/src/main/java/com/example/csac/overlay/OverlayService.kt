package com.example.csac.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.example.csac.R
import com.example.csac.models.Clicker
import kotlin.collections.ArrayList

class OverlayService : Service() {
    companion object {
        private var running = false

        fun isRunning(): Boolean {
            return running
        }
    }

    private lateinit var overlayMenu: OverlayMenu
    private lateinit var clickers: ArrayList<Clicker>

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        running = true
        val statusBarHeight = intent?.extras!!.getInt("statusBarHeight")
        clickers = intent.extras!!.getParcelableArrayList("clickers")!!
        overlayMenu = OverlayMenu(applicationContext, clickers, statusBarHeight)
        makeNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    // Don't bind this service to anything
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        running = false
        overlayMenu.onDestroy()
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
            val builder = Notification.Builder(applicationContext, "csac_overlay")
            startForeground(1, builder.build())
        }
    }
}