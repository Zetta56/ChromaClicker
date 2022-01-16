package com.example.csac.overlay

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.abs

class Draggable(
    private val windowManager: WindowManager,
    private val layoutParams: WindowManager.LayoutParams,
    private val view: View
) : View.OnTouchListener {

    private val moveThreshold = 25
    private var offsetX = 0f
    private var offsetY = 0f
    private var initialX = 0f
    private var initialY = 0f
    private var dragging = false

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        return when(p1?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dragging = false
                offsetX = layoutParams.x - p1.rawX
                offsetY = layoutParams.y - p1.rawY
                initialX = p1.rawX
                initialY = p1.rawY
                true
            }
            MotionEvent.ACTION_MOVE -> {
                val moved: Boolean = abs(p1.rawX - initialX) > moveThreshold || abs(p1.rawY - initialY) > moveThreshold
                if(!dragging && moved) {
                    dragging = true
                }
                layoutParams.x = (p1.rawX + offsetX).toInt()
                layoutParams.y = (p1.rawY + offsetY).toInt()
                windowManager.updateViewLayout(view, layoutParams)
                true
            }
            MotionEvent.ACTION_UP -> {
                if(!dragging) {
                    p0!!.performClick()
                }
                true
            }
            else -> false
        }
    }
}