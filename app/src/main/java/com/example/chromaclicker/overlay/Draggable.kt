package com.example.chromaclicker.overlay

import android.content.res.Resources
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.example.chromaclicker.clamp
import kotlin.math.abs

class Draggable(
    private val windowManager: WindowManager,
    private val layoutParams: WindowManager.LayoutParams,
    private val view: View,
    private val onActionUp: (() -> Unit)? = null
) : View.OnTouchListener {

    private val displayMetrics = Resources.getSystem().displayMetrics
    private val moveThreshold = 25
    private var offsetX = 0
    private var offsetY = 0
    private var initialX = 0f
    private var initialY = 0f
    private var dragging = false

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        return when(p1?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dragging = false
                // Offsets represent distance from top-left corner of layout to the cursor
                offsetX = (p1.rawX - layoutParams.x).toInt()
                offsetY = (p1.rawY - layoutParams.y).toInt()
                initialX = p1.rawX
                initialY = p1.rawY
                true
            }
            MotionEvent.ACTION_MOVE -> {
                val moved: Boolean = abs(p1.rawX - initialX) > moveThreshold || abs(p1.rawY - initialY) > moveThreshold
                if(!dragging && moved) {
                    dragging = true
                }
                // Set layout position to cursor position bounded within the screen
                layoutParams.x = clamp(p1.rawX.toInt() - offsetX, 0, displayMetrics.widthPixels - layoutParams.width)
                layoutParams.y = clamp(p1.rawY.toInt() - offsetY, 0, displayMetrics.heightPixels - layoutParams.height)
                windowManager.updateViewLayout(view, layoutParams)
                true
            }
            MotionEvent.ACTION_UP -> {
                if(!dragging) {
                    p0!!.performClick()
                }
                // Call onActionUp if it isn't null
                onActionUp?.invoke()
                true
            }
            else -> false
        }
    }
}