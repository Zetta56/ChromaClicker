package com.example.csac

import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class OverlayListener(private val overlay: OverlayService) : View.OnTouchListener, View.OnClickListener {
    private val buttonIds = arrayOf(R.id.playButton, R.id.plusButton, R.id.minusButton)
    private val moveThreshold = 25
    private var offsetX = 0f
    private var offsetY = 0f
    private var initialX = 0f
    private var initialY = 0f
    private var dragging = false

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        return when(p1?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if(dragging) {
                    dragging = false
                }
                offsetX = overlay.layoutParams.x - p1.rawX
                offsetY = overlay.layoutParams.y - p1.rawY
                initialX = p1.rawX
                initialY = p1.rawY
                true
            }
            MotionEvent.ACTION_MOVE -> {
                val moved: Boolean = abs(p1.rawX - initialX) > moveThreshold || abs(p1.rawY - initialY) > moveThreshold
                if(!dragging && p0!!.id in buttonIds && moved) {
                    dragging = true
                }
                overlay.layoutParams.x = (p1.rawX + offsetX).toInt()
                overlay.layoutParams.y = (p1.rawY + offsetY).toInt()
                overlay.windowManager.updateViewLayout(overlay.view, overlay.layoutParams)
                true
            }
            MotionEvent.ACTION_UP -> {
                if(!dragging && p0!!.id in buttonIds) {
                    p0.performClick()
                }
                true
            }
            else -> false
        }
    }

    override fun onClick(p0: View?) {
        when(p0!!.id) {
            R.id.playButton -> {
                println("play clicked")
            }
            R.id.plusButton -> {
                println("plus clicked")
            }
            R.id.minusButton -> {
                println("minus clicked")
            }
        }
    }
}