package com.example.chromaclicker.overlay

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.example.chromaclicker.getScreenHeight
import com.example.chromaclicker.getScreenWidth
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Draggable(
    private val windowManager: WindowManager,
    private val layoutParams: WindowManager.LayoutParams,
    private val target: View,
    private val onActionUpListener: (() -> Unit)? = null
) : View.OnTouchListener {

    private val moveThreshold = 25
    private var offsetX = 0
    private var offsetY = 0
    private var initialX = 0f
    private var initialY = 0f
    private var dragging = false

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        return when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> onActionDown(event)
            MotionEvent.ACTION_MOVE -> onActionMove(event)
            MotionEvent.ACTION_UP -> {
                if(!dragging) {
                    view.performClick()
                }
                // Call onActionUpListener if it isn't null
                onActionUpListener?.invoke()
                true
            }
            else -> false
        }
    }

    private fun onActionDown(event: MotionEvent): Boolean {
        dragging = false
        // Offsets represent distance from top-left corner of layout to the cursor
        offsetX = (event.rawX - layoutParams.x).toInt()
        offsetY = (event.rawY - layoutParams.y).toInt()
        initialX = event.rawX
        initialY = event.rawY
        return true
    }

    private fun onActionMove(event: MotionEvent): Boolean {
        val hasMoved: Boolean = abs(event.rawX - initialX) > moveThreshold || abs(event.rawY - initialY) > moveThreshold
        if(!dragging && hasMoved) {
            dragging = true
        }
        // Set layout position to cursor position bounded within the screen
        layoutParams.x = restrictCoordinate(event.rawX.toInt() - offsetX, getScreenWidth() - layoutParams.width)
        layoutParams.y = restrictCoordinate(event.rawY.toInt() - offsetY, getScreenHeight() - layoutParams.height)
        windowManager.updateViewLayout(target, layoutParams)
        return true
    }

    private fun restrictCoordinate(coordinate: Int, max: Int): Int {
        return min(max(coordinate, 0), max)
    }
}