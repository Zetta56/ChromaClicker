package com.chromaclicker.app.overlay

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.chromaclicker.app.R
import com.chromaclicker.app.createChannel
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
            "enable" -> onEnable(intent)
            "update_settings" -> {
                // Pass updated settings to the overlay menu
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
     * Sets up the overlay menu with the passed [intent]'s extras. This also creates a notification
     * for this service on SDK 26+
     */
    private fun onEnable(intent: Intent) {
        val settings = intent.extras!!.getParcelable<AppSettings>("settings")!!
        clickers = intent.extras!!.getParcelableArrayList("clickers")!!
        overlayMenu = OverlayMenu(applicationContext, settings, clickers)
        // Run this as a foreground service on SDK 26+
        if(Build.VERSION.SDK_INT >= 26) {
            createChannel(applicationContext)
            val builder = Notification.Builder(applicationContext, getString(R.string.channel_id))
                .setContentTitle("Overlay is now running")
                .setSmallIcon(R.mipmap.ic_launcher)
            startForeground(1, builder.build())
        }
        running = true
    }
}