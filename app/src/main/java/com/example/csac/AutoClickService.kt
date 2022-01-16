package com.example.csac

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.example.csac.models.CircleParcel

class AutoClickService : AccessibilityService() {
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    // Runs when this service is started using an intent
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val enabled = intent.extras!!.getBoolean("enabled")
        if(enabled) {
            val circles = intent.extras!!.getParcelableArrayList<CircleParcel>("circles")!!
            startClicking(circles)
        } else if(this::handler.isInitialized && this::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // Don't do anything when receiving accessibility event
    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {}

    // Don't do anything when interrupted
    override fun onInterrupt() {}

    private fun startClicking(circles: ArrayList<CircleParcel>) {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                for(circle in circles) {
                    click(circle.x, circle.y)
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun click(x: Int, y: Int) {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())

        val builder = GestureDescription.Builder()
        val strokeDescription = GestureDescription.StrokeDescription(path, 0, 1)
        builder.addStroke(strokeDescription)
        dispatchGesture(builder.build(), null, null)
        println(x.toFloat())
        println(y.toFloat())
    }
}