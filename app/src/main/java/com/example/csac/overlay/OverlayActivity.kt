package com.example.csac.overlay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.csac.autoclick.AutoClickService
import com.example.csac.models.Clicker
import kotlin.collections.ArrayList

class OverlayActivity : AppCompatActivity() {

    private lateinit var autoClickIntent: Intent
    private lateinit var overlayMenu: OverlayMenu
    private lateinit var clickers: ArrayList<Clicker>
    private var clickerViews = mutableListOf<ClickerView>()
    private var statusBarHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        clickers = intent?.extras!!.getParcelableArrayList("clickers")!!
        statusBarHeight = intent.extras!!.getInt("statusBarHeight")

        autoClickIntent = Intent(applicationContext, AutoClickService::class.java)
        if(AutoClickService.instance?.projection == null) {
            requestProjection()
        }
        overlayMenu = OverlayMenu(this, clickers, clickerViews, autoClickIntent, statusBarHeight)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if(intent?.action == "finish") {
            finish()
        }
    }

    override fun onDestroy() {
        overlayMenu.onDestroy()
        clickerViews.forEach { clickerView -> clickerView.onDestroy(windowManager) }
        autoClickIntent.putExtra("enabled", false)
        applicationContext.startService(autoClickIntent)
        super.onDestroy()
    }

    private fun requestProjection() {
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val intent = Intent(applicationContext, AutoClickService::class.java)
                intent.action = "receive_projection"
                intent.putExtra("projectionResult", result)
                startService(intent)
            }
        }
        activityLauncher.launch(projectionManager.createScreenCaptureIntent())
    }
}