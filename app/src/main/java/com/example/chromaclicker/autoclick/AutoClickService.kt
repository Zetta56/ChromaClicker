package com.example.chromaclicker.autoclick

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import androidx.activity.result.ActivityResult
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.chromaclicker.getScreenHeight
import com.example.chromaclicker.getScreenWidth
import com.example.chromaclicker.models.AppSettings
import com.example.chromaclicker.models.Clicker
import java.lang.Long.max
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * This service acts as an auto-clicker and a color detector.
 * You can launch intents to this service that have following actions:
 * - initialize: You must launch this once before launching any other intents, as this initializes
 * the bar heights and media projection. Extras: projectionResult (ActivityResult),
 * statusBarHeight (Int)
 * - toggle_clicker: Enables and disables the auto-clicker. Extras: enabled (Boolean), clickers
 * (ArrayList<Clickers>), settings (AppSettings)
 * - send_pixel_color: This sends the specified pixel's color to broadcast receivers listening
 * to the intent filter "receive_pixel_color". Extras: x (Int), y (Int)
 */
class AutoClickService : AccessibilityService() {
    var projection: MediaProjection? = null
    private var statusBarHeight = 0
    private var navbarHeight = 0
    // Get the message handler for this app's main thread
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var settings: AppSettings? = null
    private var clickRunnable: Runnable? = null
    private var detectRunnable: Runnable? = null
    // This service's view of the screen (to be used in color detection)
    private var screenshot: Bitmap? = null
    // ImageReader and VirtualDisplay are attributes to avoid early garbage collection
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var screenshotting = false

    // Make this service accessible to other classes using a "static" field
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
                "initialize" -> initialize(intent)
                "toggle_clicker" -> toggleRunners(intent)
                "send_pixel_color" -> getPixelColor(intent)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Parses the passed intent and initializes this class's [navbarHeight], [statusBarHeight],
     * and [projection] attributes
     */
    private fun initialize(intent: Intent) {
        val projectionResult: ActivityResult = intent.extras!!.getParcelable("projectionResult")!!
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        // Media projection is a token that lets this app record the screen
        projection = projectionManager.getMediaProjection(projectionResult.resultCode, projectionResult.data!!)

        // Get bar heights
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        navbarHeight = if(resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
        statusBarHeight = intent.extras!!.getInt("statusBarHeight")
    }

    /**
     * Parses the passed intent and either creates or removes this service's runnables from the
     * main thread, depending on the intent's extras. This also initializes [settings].
     */
    private fun toggleRunners(intent: Intent) {
        val enabled = intent.extras!!.getBoolean("enabled")
        if(enabled) {
            val clickers = intent.extras!!.getParcelableArrayList<Clicker>("clickers")!!
            settings = intent.extras!!.getParcelable("settings")!!
            startClicking(clickers)
            startDetecting(clickers)
        } else {
            clickRunnable?.let { runnable -> handler.removeCallbacks(runnable) }
            detectRunnable?.let { runnable -> handler.removeCallbacks(runnable) }
        }
    }

    /**
     * Parses the passed [intent] and updates the [screen bitmap][screenshot]. Afterwards, this
     * broadcasts the specified pixel's color to [receivers][BroadcastReceiver] listening to
     * "receive_pixel_color"
     */
    private fun getPixelColor(intent: Intent) {
        val x = intent.extras!!.getInt("x")
        val y = intent.extras!!.getInt("y")
        captureScreen {
            val colorInt = screenshot!!.getPixel(x, y)
            // Substring starts at 3rd character to ignore alpha value
            val colorString = Integer.toHexString(colorInt).substring(2)
            val broadcast = Intent("receive_pixel_color")
            broadcast.putExtra("color", "#$colorString")
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(broadcast)
        }
    }

    /**
     * Posts the auto-clicker runnable to the app's main thread. This performs a click every x
     * seconds (specified in the app settings) at each clicker location if all its color detectors
     * match up with the screen's pixels
     *
     * If randomization is enabled in this class's [settings], this will add a randomize delays
     * between clicks.
     */
    private fun startClicking(clickers: ArrayList<Clicker>) {
        clickRunnable = object : Runnable {
            override fun run() {
                settings?.let { settings ->
                    for(clicker in clickers) {
                        if(clicker.isClicking) {
                            click(clicker.x, clicker.y)
                        }
                    }
                    // If intervals are randomized, clicks can come out between 750ms faster to 750ms slower
                    val delay = if(settings.random) Random.nextLong(-750, 750) else 0
                    handler.postDelayed(this, max(settings.clickInterval + delay, 75))
                }
            }
        }
        handler.post(clickRunnable!!)
    }

    /**
     * This dispatches a click gesture at the desired x and y coordinates.
     *
     * If randomization is enabled in this class's [settings], this will click a random pixel in the
     * clicker circle, centered at the desired xy coordinates.
     */
    private fun click(x: Float, y: Float) {
        val path = Path()
        // path.moveTo()'s origin is at the top-left of the screen (above status bar)
        if(settings?.random == true) {
            val theta = Random.nextFloat() * 2 * PI.toFloat()
            val radius = Random.nextFloat() * (settings!!.circleRadius)
            path.moveTo(x + radius * cos(theta), y + radius * sin(theta) + statusBarHeight)
        } else {
            path.moveTo(x, y + statusBarHeight)
        }

        val strokeDescription = GestureDescription.StrokeDescription(path, 0, 1)
        val builder = GestureDescription.Builder()
        builder.addStroke(strokeDescription)
        dispatchGesture(builder.build(), null, null)
    }

    /**
     * Posts the color detection runnable to the app's main thread, which updates the service's
     * [screenshot] and then records whether each of the provided clickers should be clicking. This
     * process repeats every x seconds (specified in this class's [settings])
     */
    private fun startDetecting(clickers: ArrayList<Clicker>) {
        detectRunnable = object : Runnable {
            override fun run() {
                captureScreen {
                    for(clicker in clickers) {
                        clicker.isClicking = hasMatchingDetectors(clicker)
                    }
                }
                val interval = settings?.detectInterval ?: 5000
                handler.postDelayed(this, interval.toLong())
            }
        }
        handler.post(detectRunnable!!)
    }

    /** Returns whether the on-screen pixel colors match the passed [clicker]'s detector colors. */
    private fun hasMatchingDetectors(clicker: Clicker): Boolean {
        for (detector in clicker.detectors) {
            val pixelColor = screenshot!!.getPixel(detector.x.toInt(), detector.y.toInt())
            val pixelHexString = "#" + Integer.toHexString(pixelColor).substring(2)
            if (pixelHexString != detector.color) {
                return false
            }
        }
        return true
    }

    /**
     * Creates a [VirtualDisplay] and [ImageReader] used to capture the screen. After this
     * image finishes rendering, this will update the current [screenshot], clean up, and run
     * the provided [callback] function.
     */
    private fun captureScreen(callback: () -> Unit = {}) {
        // Don't re-create reader and virtual display if the previous image is still rendering
        if(!screenshotting) {
            @SuppressLint("WrongConstant")
            // Create an image reader used to obtain images
            imageReader = ImageReader.newInstance(getScreenWidth(), getScreenHeight(), PixelFormat.RGBA_8888, 2)
            // Create a public display that cannot show other virtual displays
            val flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC or DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
            // Mirror the screen into a virtual display and render it onto an image reader
            virtualDisplay = projection!!.createVirtualDisplay("screen-mirror", imageReader!!.width,
                imageReader!!.height + navbarHeight, Resources.getSystem().displayMetrics.densityDpi,
                flags, imageReader!!.surface, null , null)
            screenshotting = true
        }
        // Runs after virtual display finishes rendering to image reader
        imageReader!!.setOnImageAvailableListener({ reader ->
            updateScreenshot(reader)
            virtualDisplay?.release()
            reader.close()
            screenshotting = false
            callback()
        }, null)
    }

    /** This formats the [image reader's][reader] image and sets the [screenshot] to it. */
    private fun updateScreenshot(reader: ImageReader) {
        // Copy image to a new bitmap
        val image = reader.acquireLatestImage()
        val plane = image.planes[0]
        val bitmap = Bitmap.createBitmap(imageReader!!.width, imageReader!!.height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(plane.buffer)
        // Crop out the top status bar
        screenshot = Bitmap.createBitmap(bitmap, 0, statusBarHeight, imageReader!!.width,
            imageReader!!.height - statusBarHeight)
        image.close()
    }
}

