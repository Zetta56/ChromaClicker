package com.chromaclicker.app.main

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.chromaclicker.app.R
import com.chromaclicker.app.models.AppSettings
import com.chromaclicker.app.models.Save
import com.chromaclicker.app.overlay.OverlayService

/** This is the main activity. It contains methods shared by multiple fragments. */
class MainActivity : AppCompatActivity() {
    private lateinit var overlayIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overlayIntent = Intent(applicationContext, OverlayService::class.java)
        setContentView(R.layout.activity_main)
    }

    /**
     * Toggles the overlay and passes the currently [selected save][selectedSave]
     * to the [OverlayService]
     */
    fun toggleOverlay(toggle: Boolean, selectedSave: Save?): Boolean {
        // If this app is missing permissions, show the permissions dialog
        if(!PermissionsDialog.hasPermissions(this)) {
            PermissionsDialog().show(supportFragmentManager, "permissions")
            return false
        // If enabling, setup and launch the overlay intent
        } else if(toggle) {
            val clickers = selectedSave?.clickers ?: arrayListOf()
            overlayIntent.action = "enable"
            overlayIntent.putParcelableArrayListExtra("clickers", ArrayList(clickers))
            overlayIntent.putExtra("settings", AppSettings(this))

            // Start a foreground service in API 26+ to prevent early destruction
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(overlayIntent)
            } else {
                startService(overlayIntent)
            }
        // If disabling, stop the overlay service
        } else {
            stopService(overlayIntent)
        }
        return true
    }
}