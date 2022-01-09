package com.example.csac

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.view.accessibility.AccessibilityEvent

class AutoClickService : AccessibilityService() {

    // Runs when this service is started using an intent
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        click()
        return super.onStartCommand(intent, flags, startId)
    }

    // Don't do anything when receiving accessibility event
    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {}

    // Don't do anything when interrupted
    override fun onInterrupt() {}

    private fun click() {
        val path = Path()
        path.moveTo(517f, 1084f)

        val builder = GestureDescription.Builder()
        builder.addStroke(GestureDescription.StrokeDescription(path, 0, 1))
        dispatchGesture(builder.build(), null, null)
    }
}