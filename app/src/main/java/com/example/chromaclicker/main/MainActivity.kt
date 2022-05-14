package com.example.chromaclicker.main

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.chromaclicker.R
import com.example.chromaclicker.autoclick.AutoClickService
import com.example.chromaclicker.models.AppSettings
import com.example.chromaclicker.models.Save
import com.example.chromaclicker.overlay.OverlayService

class MainActivity : AppCompatActivity() {
    private lateinit var projectionLauncher: ActivityResultLauncher<Intent>
    private lateinit var overlayIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overlayIntent = Intent(applicationContext, OverlayService::class.java)
        // Register callback for MainFragment's projection requests (registering here prevents
        // callback from being prematurely destroyed)
        projectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val intent = Intent(applicationContext, AutoClickService::class.java)
                val rect = Rect()
                window.decorView.getWindowVisibleDisplayFrame(rect)
                intent.action = "initialize"
                intent.putExtra("projectionResult", result)
                intent.putExtra("statusBarHeight", rect.top)
                startService(intent)
            }
        }
        setContentView(R.layout.activity_main)
    }

    internal fun toggleOverlay(toggle: Boolean, selectedSave: Save?): Boolean {
        if(!PermissionsDialog.hasPermissions(this)) {
            PermissionsDialog().show(supportFragmentManager, "permissions")
            return false
        } else if(toggle) {
            val clickers = selectedSave?.clickers ?: arrayListOf()
            overlayIntent.action = "enable"
            overlayIntent.putParcelableArrayListExtra("clickers", ArrayList(clickers))
            overlayIntent.putExtra("settings", AppSettings(this))

            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(overlayIntent)
            } else {
                startService(overlayIntent)
            }
        } else {
            stopService(overlayIntent)
        }
        return true
    }
}