package com.example.csac.overlay

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.view.*
import com.example.csac.AutoClickService
import com.example.csac.models.CircleParcel
import com.example.csac.R
import com.example.csac.createOverlayLayout
import com.example.csac.databinding.OverlayCanvasBinding
import com.example.csac.databinding.OverlayMenuBinding
import com.example.csac.setRecursiveTouchListener

// To add another view, just add it with a new layoutParams and call windowManager.addView()
class OverlayService : Service() {

    private lateinit var binding: OverlayMenuBinding
    private lateinit var autoClickIntent: Intent
    private lateinit var windowManager: WindowManager
    private var circles = mutableListOf<CircleView>()
    private var playing = false


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        autoClickIntent = Intent(applicationContext, AutoClickService::class.java)
        addMenu()
        makeNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    // Don't bind this service to anything
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    // Destroy created views when this service is stopped
    override fun onDestroy() {
        windowManager.removeView(binding.root)
        circles.forEach { circle -> circle.onDestroy(windowManager) }
        autoClickIntent.putExtra("enabled", false)
        applicationContext.startService(autoClickIntent)
        super.onDestroy()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addMenu() {
        val layoutParams = createOverlayLayout(55, 165, Gravity.START)
        binding = OverlayMenuBinding.inflate(LayoutInflater.from(applicationContext))
        windowManager.addView(binding.root, layoutParams)

        // Add event listeners
        val draggable = Draggable(windowManager, layoutParams, binding.root)
        setRecursiveTouchListener(binding.root, draggable)
        binding.playButton.setOnClickListener { toggleClicker() }
        binding.plusButton.setOnClickListener { addCircle() }
        binding.minusButton.setOnClickListener { removeCircle() }
    }

    private fun toggleClicker() {
        playing = !playing
        if(playing) {
            circles.forEach { circle -> circle.visibility = View.INVISIBLE }
            binding.playButton.setImageResource(R.drawable.pause)

            val circleParcels = ArrayList(circles.map { circle -> CircleParcel(circle) })
            autoClickIntent.putParcelableArrayListExtra("circles", circleParcels)
            autoClickIntent.putExtra("enabled", true)
            applicationContext.startService(autoClickIntent)
        } else {
            circles.forEach { circle -> circle.visibility = View.VISIBLE }
            binding.playButton.setImageResource(R.drawable.play)
            autoClickIntent.putExtra("enabled", false)
            applicationContext.startService(autoClickIntent)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addCircle() {
        val circleLayout = createOverlayLayout(60, 60)
        val circle = CircleView(applicationContext, null)
        circle.addListeners(windowManager, circleLayout, circles, binding.root)
        windowManager.addView(circle, circleLayout)
        circles += circle
    }

    private fun removeCircle() {
        if(circles.size > 0) {
            windowManager.removeView(circles[circles.lastIndex])
            circles.removeAt(circles.lastIndex)
        }
    }

    private fun makeNotification() {
        if(Build.VERSION.SDK_INT >= 26) {
            // Create notification channel for foreground service
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "csac_overlay",
                getString(R.string.overlay_notification),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setSound(null, null)
            notificationManager.createNotificationChannel(channel)

            // Send notification
            val builder = Notification.Builder(this, "csac_overlay")
            startForeground(1, builder.build())
        }
    }
}