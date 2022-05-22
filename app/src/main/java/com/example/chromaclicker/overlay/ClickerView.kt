package com.example.chromaclicker.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import com.example.chromaclicker.models.Clicker

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
        // Destroy clicker-menu if it was fully initialized
        if(this::clickerMenu.isInitialized && clickerMenu.hasWindowToken()) {
            clickerMenu.onDestroy()
        }
        windowManager.removeView(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun addListeners(windowManager: WindowManager, clicker: Clicker, layoutParams: WindowManager.LayoutParams,
                     clickerViews: MutableList<ClickerView>, overlayMenu: View) {
        setOnClickListener {
            val position = listOf(layoutParams.x.toFloat(), layoutParams.y.toFloat())
            clickerMenu = ClickerMenu(context, windowManager, clicker, position, getCenter(layoutParams), clickerViews, overlayMenu)
            // Hide other views
            clickerViews.forEach { clickerView -> clickerView.visibility = INVISIBLE }
            overlayMenu.visibility = INVISIBLE
        }

        setOnTouchListener(Draggable(windowManager, layoutParams, this, onActionUpListener={
            val center = getCenter(layoutParams)
            clicker.x = center[0]
            clicker.y = center[1]
        }))
    }

    private fun getCenter(layoutParams: WindowManager.LayoutParams): Array<Float> {
        return arrayOf(
            (layoutParams.x + layoutParams.width / 2).toFloat(),
            (layoutParams.y + layoutParams.height / 2).toFloat()
        )
    }
}