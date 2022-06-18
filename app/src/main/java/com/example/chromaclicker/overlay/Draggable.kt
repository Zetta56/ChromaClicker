package com.example.chromaclicker.overlay

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.example.chromaclicker.getScreenHeight
import com.example.chromaclicker.getScreenWidth
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * This class makes the [view][target] draggable by updating its [layoutParams] and refreshing the
 * [windowManager] whenever the user moves their cursor. This will call [onActionUpListener]
 * when the user releases their cursor.
 */
class Draggable(
    private val windowManager: WindowManager,
    private val layoutParams: WindowManager.LayoutParams,
    private val target: View,
    private val onActionUpListener: (() -> Unit)? = null
) : View.OnTouchListener {

    // Minimum pixels moved for user to be considered dragging
    private val moveThreshold = 25
    private var offsetX = 0
    private var offsetY = 0
    private var initialX = 0f
    private var initialY = 0f
    private var dragging = false

    // performClick() is called in the onActionUp function
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        // Decide what to do based on the action type
        return when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> onActionDown(event)
            MotionEvent.ACTION_MOVE -> onActionMove(event)
            MotionEvent.ACTION_UP -> onActionUp(view)
            // Notify other listeners that this action wasn't handled by returning false
            else -> false
        }
    }

    /** When pressing down, initialize flags, cursor offsets, and initial cursor positions */
    private fun onActionDown(event: MotionEvent): Boolean {
        dragging = false
        // Move view within screen bounds, in case its original position was offscreen (due to a screen rotation)
        updatePosition(layoutParams.x, layoutParams.y)
        // Offsets represent the distance from the top-left corner of its layout to the cursor
        offsetX = (event.rawX - layoutParams.x).toInt()
        offsetY = (event.rawY - layoutParams.y).toInt()
        // Initial position is used to check if the user is dragging
        initialX = event.rawX
        initialY = event.rawY
        return true
    }

    /** When moving the cursor, move the view's position and check if the user intentionally dragged */
    private fun onActionMove(event: MotionEvent): Boolean {
        // Set dragging to true if the user moved their cursor further than the moveThreshold
        val hasMoved: Boolean = abs(event.rawX - initialX) > moveThreshold || abs(event.rawY - initialY) > moveThreshold
        if(!dragging && hasMoved) {
            dragging = true
        }
        updatePosition(event.rawX.toInt() - offsetX, event.rawY.toInt() - offsetY)
        return true
    }

    /**
     * When releasing the cursor, perform a click on the touched view if the user was not dragging.
     * This will also call [onActionUpListener].
     */
    private fun onActionUp(view: View): Boolean {
        if(!dragging) {
            view.performClick()
        }
        // Call onActionUpListener if it isn't null
        onActionUpListener?.invoke()
        return true
    }

    /**
     * Update the [layoutParams] position to the desired [x] and [y] coordinates, bounded
     * within the screen.
     */
    private fun updatePosition(x: Int, y: Int) {
        layoutParams.x = min(max(x, 0), getScreenWidth() - layoutParams.width)
        layoutParams.y = min(max(y, 0), getScreenHeight() - layoutParams.height)
        windowManager.updateViewLayout(target, layoutParams)
    }
}