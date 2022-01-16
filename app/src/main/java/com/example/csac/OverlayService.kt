package com.example.csac

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import com.example.csac.databinding.OverlayMenuBinding

// To add another view, just add it with a new layoutParams and call windowManager.addView()
class OverlayService : Service() {

    private lateinit var menu: OverlayMenuBinding
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
        windowManager.removeView(menu.root)
        circles.forEach { circle -> windowManager.removeView(circle) }
        autoClickIntent.putExtra("enabled", false)
        applicationContext.startService(autoClickIntent)
        super.onDestroy()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addMenu() {
        val layoutParams = createOverlayLayout(55, 165, Gravity.START)
        menu = OverlayMenuBinding.inflate(LayoutInflater.from(applicationContext))
        windowManager.addView(menu.root, layoutParams)

        // Add event listeners
        val draggable = Draggable(windowManager, layoutParams, menu.root)
        menu.root.setOnTouchListener(draggable)
        menu.playButton.setOnTouchListener(draggable)
        menu.plusButton.setOnTouchListener(draggable)
        menu.minusButton.setOnTouchListener(draggable)
        menu.playButton.setOnClickListener { toggleClicker() }
        menu.plusButton.setOnClickListener { addCircle() }
        menu.minusButton.setOnClickListener { removeCircle() }
    }

    private fun toggleClicker() {
        playing = !playing
        if(playing) {
            circles.forEach { circle -> circle.visibility = View.INVISIBLE }
            menu.playButton.setImageResource(R.drawable.pause)

            val circleParcels = ArrayList(circles.map { circle -> CircleParcel(circle) })
            autoClickIntent.putParcelableArrayListExtra("circles", circleParcels)
            autoClickIntent.putExtra("enabled", true)
            applicationContext.startService(autoClickIntent)
        } else {
            circles.forEach { circle -> circle.visibility = View.VISIBLE }
            menu.playButton.setImageResource(R.drawable.play)
            autoClickIntent.putExtra("enabled", false)
            applicationContext.startService(autoClickIntent)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addCircle() {
        val circle = CircleView(applicationContext)
        val layoutParams = createOverlayLayout(60, 60, Gravity.NO_GRAVITY)
        circle.setOnTouchListener(Draggable(windowManager, layoutParams, circle))
        windowManager.addView(circle, layoutParams)
        circles += circle
    }

    private fun removeCircle() {
        println("removing")
    }

    private fun createOverlayLayout(width: Int, height: Int, gravity: Int): WindowManager.LayoutParams {
        val layoutParams = WindowManager.LayoutParams()
        // Convert width and height from pixels to dp
        layoutParams.width = (width * applicationContext.resources.displayMetrics.density).toInt()
        layoutParams.height = (height * applicationContext.resources.displayMetrics.density).toInt()
        // Display this on top of other applications
        layoutParams.type = if(Build.VERSION.SDK_INT >= 26) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("Deprecation")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        // Don't grab input focus
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        // Make the underlying application visible through any transparent sections
        layoutParams.format = PixelFormat.TRANSLUCENT
        // Position layout using gravity
        layoutParams.gravity = gravity
        return layoutParams
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