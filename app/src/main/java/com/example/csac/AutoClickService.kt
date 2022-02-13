package com.example.csac

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import androidx.activity.result.ActivityResult
import com.example.csac.models.Clicker
import com.example.csac.overlay.ProjectionActivity
import java.lang.Float.max

class AutoClickService : AccessibilityService() {
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var projection: MediaProjection

    // Runs when this service is started using an intent
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null && intent.action != null) {
            when (intent.action) {
                "toggle" -> {
                    val enabled = intent.extras!!.getBoolean("enabled")
                    if(enabled) {
                        val clickers = intent.extras!!.getParcelableArrayList<Clicker>("clickers")!!
                        startClicking(clickers)
                    } else {
                        stopClicking()
                    }
                }
                "request_projection" -> {
                    val projectionResult: ActivityResult = intent.extras!!.getParcelable("projectionResult")!!
                    val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    // Media projection is a token that lets this app record the screen
                    projection = projectionManager.getMediaProjection(projectionResult.resultCode, projectionResult.data!!)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // Don't do anything when receiving accessibility event
    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {}

    // Don't do anything when interrupted
    override fun onInterrupt() {}

    private fun startClicking(clickers: ArrayList<Clicker>) {
//        val screenShotIntent = Intent(applicationContext, ProjectionRequester::class.java)
//        screenShotIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(screenShotIntent)
        takeScreenshot()
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                for(clicker in clickers) {
                    Resources.getSystem().displayMetrics.widthPixels
                    click(clicker.x, clicker.y)
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun stopClicking() {
        if(this::handler.isInitialized && this::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
    }

    private fun click(x: Float, y: Float) {
        val path = Path()
        // max function prevents x and y from being negative
        path.moveTo(max(0f, x), max(0f, y))

        val builder = GestureDescription.Builder()
        val strokeDescription = GestureDescription.StrokeDescription(path, 0, 1)
        builder.addStroke(strokeDescription)
        dispatchGesture(builder.build(), null, null)
        println(x)
        println(y)
    }

    @SuppressLint("WrongConstant")
    fun takeScreenshot() {
        if(!this::projection.isInitialized) {
            ProjectionActivity.launch(applicationContext)
            return
        }

        val displayMetrics = Resources.getSystem().displayMetrics
        // Image reader receives images from a virtual display
        val imageReader = ImageReader.newInstance(displayMetrics.widthPixels, displayMetrics.heightPixels, PixelFormat.RGBA_8888, 2)
        // Create a display that can mirror the screen, but cannot show other virtual displays
        val flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC or DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
        val virtualDisplay = projection.createVirtualDisplay("screen-mirror", displayMetrics.widthPixels,
            displayMetrics.heightPixels, displayMetrics.densityDpi, flags, imageReader.surface, null , null)

        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            val plane = image.planes[0]
            val rowPadding = plane.rowStride - plane.pixelStride * displayMetrics.widthPixels
            val bitmap = Bitmap.createBitmap(displayMetrics.widthPixels + (rowPadding.toFloat() /
                plane.pixelStride).toInt(), displayMetrics.heightPixels, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(plane.buffer)

            println(Color.red(bitmap.getPixel(200, 200)))
            println(Color.green(bitmap.getPixel(200, 200)))
            println(Color.blue(bitmap.getPixel(200, 200)))

            val fos = openFileOutput("testBitmap", MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos)
            fos.close()

            image.close()
            virtualDisplay.release()
            reader.close()
        }, null)
    }
}