package com.example.csac

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.TYPE_TOAST
import androidx.annotation.RequiresApi

class OverlayService : Service() {
    lateinit var windowManager: WindowManager
    lateinit var view: View

    @SuppressLint("InflateParams")
    override fun onCreate() {
        super.onCreate()
        val layoutParams = WindowManager.LayoutParams(
            // Set the width and height to wrap_content
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            // Display this on top of other application windows
            WindowManager.LayoutParams.TYPE_TOAST,
            // Don't grab input focus
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            // Make the underlying application window visible through any transparent sections
            PixelFormat.TRANSLUCENT
        )
        view = LayoutInflater.from(this).inflate(R.layout.overlay, null)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(view, layoutParams)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(view)
    }
}