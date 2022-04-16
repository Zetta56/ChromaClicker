package com.example.csac.main


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.csac.R
import com.example.csac.autoclick.AutoClickService
import com.example.csac.models.Clicker
import com.example.csac.models.Save
import com.example.csac.overlay.OverlayService


class MainActivity : AppCompatActivity() {
    lateinit var projectionLauncher: ActivityResultLauncher<Intent>
    lateinit var overlayIntent: Intent

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
        if(!hasPermissions()) {
            return false
        } else if(toggle) {
            val clickers = selectedSave?.clickers?.map { c -> Clicker(c) } ?: arrayListOf()
            overlayIntent.putParcelableArrayListExtra("clickers", ArrayList(clickers))
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

    private fun hasPermissions(): Boolean {
        if(!Settings.canDrawOverlays(this)) {
            // Redirect to overlay permission screen for this app
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${packageName}")
            )
            applicationContext.startActivity(intent)
            return false
        }
        if(Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED) == 0) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            applicationContext.startActivity(intent)
            return false
        }
        if(AutoClickService.instance?.projection == null) {
            val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
            return false
        }
        return true
    }
}