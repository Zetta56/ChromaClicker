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

/** This view displays a circle. You can optionally add logic to this by calling [addListeners] */
class Circle(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
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
        // This isn't in the constructor, since measured properties can only be used after onMeasure is called
        val centerX = (measuredWidth / 2).toFloat()
        val centerY = (measuredHeight / 2).toFloat()
        // Draw the outer circle
        canvas?.drawCircle(centerX, centerY, centerX - borderPaint.strokeWidth, borderPaint)
        // Draw the center
        canvas?.drawCircle(centerX, centerY, 15f, centerPaint)
    }

    fun onDestroy(windowManager: WindowManager) {
        // Destroy the clicker menu if it was created
        if(this::clickerMenu.isInitialized && clickerMenu.hasWindowToken()) {
            clickerMenu.onDestroy()
        }
        windowManager.removeView(this)
    }

    /**
     * This adds optional logic to this circle.
     * - When dragged, this circle will move by updating the [windowManager] with a new set of
     * [layoutParams]
     * - When clicked, this will add a [clickerMenu] managing a [clicker] to the [windowManager].
     * This will also hide other [circles] and the [overlayMenu]
     */
    @SuppressLint("ClickableViewAccessibility")
    fun addListeners(windowManager: WindowManager, clicker: Clicker, layoutParams: WindowManager.LayoutParams,
                     circles: MutableList<Circle>, overlayMenu: View) {
        // Open a clicker menu when this is clicked
        setOnClickListener {
            val position = listOf(layoutParams.x.toFloat(), layoutParams.y.toFloat())
            clickerMenu = ClickerMenu(context, windowManager, clicker, position, getCenter(layoutParams)) {
                circles.forEach { circle -> circle.visibility = VISIBLE }
                overlayMenu.visibility = VISIBLE
            }
            // Hide other circles
            circles.forEach { circle -> circle.visibility = INVISIBLE }
            overlayMenu.visibility = INVISIBLE
        }

        // Make this circle draggable
        setOnTouchListener(Draggable(windowManager, layoutParams, this, onActionUpListener={
            // Update this circle's associated clicker position after the user finishes dragging
            val center = getCenter(layoutParams)
            clicker.x = center[0]
            clicker.y = center[1]
        }))
    }

    /** Returns this circle's center, adjusted for its radius */
    private fun getCenter(layoutParams: WindowManager.LayoutParams): Array<Float> {
        return arrayOf(
            (layoutParams.x + layoutParams.width / 2).toFloat(),
            (layoutParams.y + layoutParams.height / 2).toFloat()
        )
    }
}