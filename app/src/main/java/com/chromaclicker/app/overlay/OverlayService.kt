package com.chromaclicker.app.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.chromaclicker.app.R
import com.chromaclicker.app.models.AppSettings
import com.chromaclicker.app.models.Clicker
import kotlin.collections.ArrayList

/** This service displays overlay windows.
 *  You can launch intents to this service that have following actions:
 *  - enable: Displays the overlay menu and creates a notification. Extras: settings (AppSettings),
 *  clickers (ArrayList<Clicker>)
 *  - update_settings: Reloads the overlay menu with updated settings. Extras: settings (AppSettings)
 */
class OverlayService : Service() {

    companion object {
        private var running = false

        /** Returns whether an instance of this service is running */
        fun isRunning(): Boolean {
            return running
        }
    }

    private lateinit var clickers: ArrayList<Clicker>
    private var overlayMenu: OverlayMenu? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            // Pass intent data to the overlay menu and make a notification
            "enable" -> {
                val settings = intent.extras!!.getParcelable<AppSettings>("settings")!!
                clickers = intent.extras!!.getParcelableArrayList("clickers")!!
                overlayMenu = OverlayMenu(applicationContext, settings, clickers)
                makeNotification()
                running = true
            }
            // Pass updated settings to the overlay menu
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

    /**
     * Sends a notification to the user. This must be called for this service to be ran as a
     * foreground service in API 26+
     */
    private fun makeNotification() {
        if(Build.VERSION.SDK_INT >= 26) {
            // Create notification channel for this foreground service
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