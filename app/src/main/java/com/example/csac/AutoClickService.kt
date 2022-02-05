package com.example.csac

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.content.res.Resources
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.example.csac.models.Clicker

class AutoClickService : AccessibilityService() {
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    // Runs when this service is started using an intent
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val enabled = intent?.extras!!.getBoolean("enabled")
        if(enabled) {
            val clickers = intent.extras!!.getParcelableArrayList<Clicker>("clickers")!!
            startClicking(clickers)
        // Stop auto clicker if it has already started and user wants to disable it
        } else if(this::handler.isInitialized && this::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // Don't do anything when receiving accessibility event
    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {}

    // Don't do anything when interrupted
    override fun onInterrupt() {}

    private fun startClicking(clickers: ArrayList<Clicker>) {
        // Offsets used to get coordinates from top-left instead of screen center
        val offsetX = Resources.getSystem().displayMetrics.widthPixels / 2
        val offsetY = Resources.getSystem().displayMetrics.heightPixels / 2

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                for(clicker in clickers) {
                    Resources.getSystem().displayMetrics.widthPixels
                    click(clicker.x + offsetX, clicker.y + offsetY)
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun click(x: Float, y: Float) {
        val path = Path()
        path.moveTo(x, y)

        val builder = GestureDescription.Builder()
        val strokeDescription = GestureDescription.StrokeDescription(path, 0, 1)
        builder.addStroke(strokeDescription)
        dispatchGesture(builder.build(), null, null)
        println(x)
        println(y)
    }
}