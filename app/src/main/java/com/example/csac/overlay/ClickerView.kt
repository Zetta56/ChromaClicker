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
import com.example.csac.models.Clicker
import com.example.csac.toDP
import com.example.csac.toPixels

class ClickerView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private lateinit var clickerMenu: ClickerMenu
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
        if(this::clickerMenu.isInitialized && clickerMenu.hasWindowToken()) {
            clickerMenu.onDestroy()
        }
        windowManager.removeView(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun addListeners(windowManager: WindowManager, clicker: Clicker, layoutParams: WindowManager.LayoutParams,
                     clickerViews: MutableList<ClickerView>, overlayMenu: View) {
        setOnClickListener {
//            val center = getCoordinates(this, true)
            val clickerCenter = arrayOf(
                (layoutParams.x + this.width / 2).toFloat(),
                (layoutParams.y + this.height / 2).toFloat()
            )
            val drawing = addDrawing(windowManager, clickerCenter)
            clickerMenu = ClickerMenu(context, windowManager, clicker, drawing, clickerCenter, clickerViews, overlayMenu)

            // Hide other views
            clickerViews.forEach { clickerView -> clickerView.visibility = INVISIBLE }
            overlayMenu.visibility = INVISIBLE
        }
        setOnTouchListener(Draggable(windowManager, layoutParams, this, onActionUp={
            clicker.x = layoutParams.x.toFloat()
            clicker.y = layoutParams.y.toFloat()
        }))
    }

    private fun addDrawing(windowManager: WindowManager, clickerCenter: Array<Float>): ViewGroup {
        val drawing = FrameLayout(context, null)
        val clickerView = ClickerView(context, null)
        drawing.addView(clickerView)

        // Set clicker view dimensions and position
        clickerView.x = clickerCenter[0]
        clickerView.y = clickerCenter[1]
        clickerView.layoutParams.width = toPixels(60)
        clickerView.layoutParams.height = toPixels(60)

        // Make drawing window
        val displayMetrics = context.resources.displayMetrics
        val drawingLayout = createOverlayLayout(toDP(displayMetrics.widthPixels), toDP(displayMetrics.heightPixels), touchable=false)
        windowManager.addView(drawing, drawingLayout)

        return drawing
    }
}