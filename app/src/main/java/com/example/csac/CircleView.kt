package com.example.csac

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CircleView(context: Context?) : View(context) {
    private val borderPaint = Paint()
    private val fillPaint = Paint()
    private val centerPaint = Paint()

    init {
        borderPaint.color = Color.parseColor("#a02256b5")
        fillPaint.color = Color.parseColor("#a02697ed")
        centerPaint.color = Color.parseColor("#2256b5")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(halve(measuredWidth), halve(measuredHeight), halve(measuredWidth), borderPaint)
        canvas?.drawCircle(halve(measuredWidth), halve(measuredHeight), halve(measuredWidth) - 20f, fillPaint)
        canvas?.drawCircle(halve(measuredWidth), halve(measuredHeight), 15f, fillPaint)
    }

    private fun halve(num: Int): Float {
        return (num / 2).toFloat()
    }
}