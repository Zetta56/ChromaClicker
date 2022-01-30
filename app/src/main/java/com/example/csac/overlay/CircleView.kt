package com.example.csac.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.example.csac.createOverlayLayout
import com.example.csac.databinding.OverlayCanvasBinding
import com.example.csac.getCoordinates

class CircleView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private lateinit var circleMenu: CircleMenu
    private val borderPaint = Paint()
    private val centerPaint = Paint()

    init {
        borderPaint.color = Color.parseColor("#2256b5")
        borderPaint.strokeWidth = 15f
        borderPaint.style = Paint.Style.STROKE
        centerPaint.color = Color.parseColor("#2256b5")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // Measured properties can only be used after onMeasure finishes running
        val centerX = (measuredWidth / 2).toFloat()
        val centerY = (measuredHeight / 2).toFloat()
        canvas?.drawCircle(centerX, centerY, centerX - borderPaint.strokeWidth, borderPaint)
        canvas?.drawCircle(centerX, centerY, 15f, centerPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun addListeners(windowManager: WindowManager, layoutParams: WindowManager.LayoutParams,
                     circles: MutableList<CircleView>, menuView: View) {
        setOnTouchListener(Draggable(windowManager, layoutParams, this))
        setOnClickListener {
            // Add overlay canvas
            val displayMetrics = context.resources.displayMetrics
            val canvasLayout = createOverlayLayout(displayMetrics.widthPixels,
                displayMetrics.heightPixels, touchable=false, unit="px")
            val overlayCanvas = OverlayCanvasBinding.inflate(LayoutInflater.from(context))
            overlayCanvas.surface.setZOrderMediaOverlay(true)
            overlayCanvas.surface.holder.setFormat(PixelFormat.TRANSLUCENT)
            windowManager.addView(overlayCanvas.root, canvasLayout)

            // Position canvas circle
            val circlePosition = getCoordinates(this)
            overlayCanvas.circle.x = circlePosition[0]
            overlayCanvas.circle.y = circlePosition[1]

            // Add circleMenu
            val circleCenter = getCoordinates(this, true)
            circleMenu = CircleMenu(context, windowManager, circles, circleCenter, menuView, overlayCanvas)

            // Hide other views
            circles.forEach { circle -> circle.visibility = INVISIBLE }
            menuView.visibility = INVISIBLE
        }
    }

    fun onDestroy(windowManager: WindowManager) {
        if(this::circleMenu.isInitialized && circleMenu.hasWindowToken()) {
            circleMenu.onDestroy()
        }
        windowManager.removeView(this)
    }
}