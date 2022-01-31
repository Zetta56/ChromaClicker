package com.example.csac.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.example.csac.createOverlayLayout
import com.example.csac.getCoordinates
import com.example.csac.toDP
import com.example.csac.toPixels

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

    fun onDestroy(windowManager: WindowManager) {
        if(this::circleMenu.isInitialized && circleMenu.hasWindowToken()) {
            circleMenu.onDestroy()
        }
        windowManager.removeView(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun addListeners(windowManager: WindowManager, layoutParams: WindowManager.LayoutParams,
                     circles: MutableList<CircleView>, overlayMenu: View) {
        setOnTouchListener(Draggable(windowManager, layoutParams, this))
        setOnClickListener {
            val circleCenter = getCoordinates(this, true)
            val drawing = addDrawing(windowManager)
            circleMenu = CircleMenu(context, windowManager, drawing, circleCenter, circles, overlayMenu)

            // Hide other views
            circles.forEach { circle -> circle.visibility = INVISIBLE }
            overlayMenu.visibility = INVISIBLE
        }
    }

    private fun addDrawing(windowManager: WindowManager): ViewGroup {
        val drawing = FrameLayout(context, null)
        val circle = CircleView(context, null)
        drawing.addView(circle)

        // Set circle dimensions and position
        val circlePosition = getCoordinates(this)
        circle.x = circlePosition[0]
        circle.y = circlePosition[1]
        circle.layoutParams.width = toPixels(60)
        circle.layoutParams.height = toPixels(60)

        // Make drawing window
        val displayMetrics = context.resources.displayMetrics
        val drawingLayout = createOverlayLayout(toDP(displayMetrics.widthPixels), toDP(displayMetrics.heightPixels), touchable=false)
        windowManager.addView(drawing, drawingLayout)
        return drawing
    }
}