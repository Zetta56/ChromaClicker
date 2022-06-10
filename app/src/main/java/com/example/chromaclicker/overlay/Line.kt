package com.example.chromaclicker.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * This view displays a line. You can set its [color], the [x][startX] and [y][startY] positions
 * of its first endpoint, and the [x][endX] and [y][endY] positions of its second endpoint.
 */
class Line(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint()
    private val path = Path()
    var startX = 0f
    var startY = 0f
    var endX = 0f
    var endY = 0f
    var color = "#000000"

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        path.reset()
        paint.color = Color.parseColor(color)
        // Draw a line
        path.moveTo(startX, startY)
        path.lineTo(endX, endY)
        canvas?.drawPath(path, paint)
    }
}