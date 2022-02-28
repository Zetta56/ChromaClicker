package com.example.csac

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.activity.result.ActivityResult
import com.example.csac.models.Clicker

class AutoClickService : AccessibilityService() {
    var projection: MediaProjection? = null
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var clickerStates: BooleanArray? = null
    private var clickRunnable: Runnable? = null
    private var detectRunnable: Runnable? = null
    private var screenBitmap: Bitmap? = null
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var screenshotting = false
    private var statusBarHeight = 0

    // Make this service accessible to other classes using a static field
    companion object {
        var instance: AutoClickService? = null
    }

    override fun onServiceConnected() {
        instance = this
        super.onServiceConnected()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    // Don't do anything when receiving accessibility event
    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {}

    // Don't do anything when interrupted
    override fun onInterrupt() {}

    // Runs when this service is started using an intent
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null && intent.action != null) {
            when (intent.action) {
                "toggle" -> {
                    val enabled = intent.extras!!.getBoolean("enabled")
                    if(enabled) {
                        val clickers = intent.extras!!.getParcelableArrayList<Clicker>("clickers")!!
                        statusBarHeight = intent.extras!!.getInt("statusBarHeight")
                        clickerStates = BooleanArray(clickers.size)
                        startRunners(clickers)
                    } else {
                        stopRunners()
                    }
                }
                "send_projection" -> {
                    val projectionResult: ActivityResult = intent.extras!!.getParcelable("projectionResult")!!
                    val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    // Media projection is a token that lets this app record the screen
                    projection = projectionManager.getMediaProjection(projectionResult.resultCode, projectionResult.data!!)
                }
                "get_pixel_color" -> {
                    val x = intent.extras!!.getInt("x")
                    val y = intent.extras!!.getInt("y")
                    updateScreenBitmap {
                        val colorInt = screenBitmap!!.getPixel(x, y + statusBarHeight)
                        val colorString = Integer.toHexString(colorInt).substring(2)
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = ClipData.newPlainText(colorString, colorString)
                        clipboard.setPrimaryClip(clipData)
                        Toast.makeText(applicationContext, "Copied to clipboard: $colorString", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startRunners(clickers: ArrayList<Clicker>) {
        clickRunnable = object : Runnable {
            override fun run() {
                for((index, clicker) in clickers.withIndex()) {
                    if(clickerStates != null && clickerStates!![index]) {
                        click(clicker.x, clicker.y)
                    }
                }
                handler.postDelayed(this, 1000)
            }
        }
        detectRunnable = object : Runnable {
            override fun run() {
                updateScreenBitmap {
                    updateClickerStates(clickers)
                }
                handler.postDelayed(this, 5000)
            }
        }
        handler.post(clickRunnable!!)
        handler.post(detectRunnable!!)
    }

    private fun stopRunners() {
        if(clickRunnable != null) {
            handler.removeCallbacks(clickRunnable!!)
        }
        if(detectRunnable != null) {
            handler.removeCallbacks(detectRunnable!!)
        }
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

    private fun updateClickerStates(clickers: ArrayList<Clicker>) {
        if(clickerStates == null) {
            return
        }
        for((index, clicker) in clickers.withIndex()) {
            var state = true
            for(detector in clicker.detectors) {
                val pixelColor = screenBitmap?.getPixel(detector.x.toInt(), detector.y.toInt() + statusBarHeight)
                if(pixelColor != Color.parseColor(detector.color)) {
                    state = false
                    break
                }
            }
            clickerStates!![index] = state
        }
    }

    @SuppressLint("WrongConstant")
    private fun updateScreenBitmap(callback: () -> Unit = {}) {
        val displayMetrics = Resources.getSystem().displayMetrics
        // ImageReader and VirtualDisplay are stored as properties to avoid garbage-collection/early-destruction
        if(!screenshotting) {
            // Image reader receives images from a virtual display
            imageReader = ImageReader.newInstance(displayMetrics.widthPixels, displayMetrics.heightPixels, PixelFormat.RGBA_8888, 2)
            val flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC or DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
            // Create a display that can mirror the screen, but cannot show other virtual displays
            virtualDisplay = projection!!.createVirtualDisplay("screen-mirror", displayMetrics.widthPixels,
                displayMetrics.heightPixels, displayMetrics.densityDpi, flags, imageReader!!.surface, null , null)
            screenshotting = true
        }

        imageReader!!.setOnImageAvailableListener({ reader ->
            // Copy read image to a new bitmap
            val image = reader.acquireLatestImage()
            val plane = image.planes[0]
            val rowPadding = plane.rowStride - plane.pixelStride * displayMetrics.widthPixels
            val bitmap = Bitmap.createBitmap(displayMetrics.widthPixels + (rowPadding.toFloat() /
                plane.pixelStride).toInt(), displayMetrics.heightPixels, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(plane.buffer)
            screenBitmap = bitmap
            callback()

//             val fos = openFileOutput("testBitmap", MODE_PRIVATE)
//             bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos)
//             fos.close()

            // Cleanup
            image.close()
            virtualDisplay?.release()
            reader.close()
            screenshotting = false
        }, null)
    }
}

