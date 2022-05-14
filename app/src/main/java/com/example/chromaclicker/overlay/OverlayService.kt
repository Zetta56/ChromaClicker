package com.example.chromaclicker.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.example.chromaclicker.R
import com.example.chromaclicker.models.AppSettings
import com.example.chromaclicker.models.Clicker
import kotlin.collections.ArrayList

class OverlayService : Service() {

    companion object {
        private var running = false

        fun isRunning(): Boolean {
            return running
        }
    }

    private lateinit var clickers: ArrayList<Clicker>
    private var overlayMenu: OverlayMenu? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            "enable" -> {
                val settings = intent.extras!!.getParcelable<AppSettings>("settings")!!
                clickers = intent.extras!!.getParcelableArrayList("clickers")!!
                overlayMenu = OverlayMenu(applicationContext, settings, clickers)
                makeNotification()
                running = true
            }
            "update_settings" -> {
                val settings = intent.extras!!.getParcelable<AppSettings>("settings")!!
                overlayMenu?.updateSettings(settings)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    // Don't bind this service to anything
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        running = false
        overlayMenu?.onDestroy()
        super.onDestroy()
    }

    private fun makeNotification() {
        if(Build.VERSION.SDK_INT >= 26) {
            // Create notification channel for foreground service
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                getString(R.string.channel_id),
                getString(R.string.overlay_notification),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setSound(null, null)
            notificationManager.createNotificationChannel(channel)

            // Send notification
            val builder = Notification.Builder(applicationContext, getString(R.string.channel_id))
            startForeground(1, builder.build())
        }
    }
}