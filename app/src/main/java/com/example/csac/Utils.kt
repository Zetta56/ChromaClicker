package com.example.csac

import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

fun createOverlayLayout(width: Int, height: Int, gravity: Int = Gravity.NO_GRAVITY,
        focusable: Boolean = false, touchable: Boolean = true): WindowManager.LayoutParams {
    val layoutParams = WindowManager.LayoutParams()
    // Convert width and height from dp to pixels
    layoutParams.width = toPixels(width)
    layoutParams.height = toPixels(height)
    // Display this on top of other applications
    layoutParams.type = if(Build.VERSION.SDK_INT >= 26) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
        @Suppress("Deprecation")
        WindowManager.LayoutParams.TYPE_PHONE
    }
    // Conditionally add bitwise flags
    if(!focusable) {
        layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    }
    if(!touchable) {
        layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
    }
    // Make the underlying application visible through any transparent sections
    layoutParams.format = PixelFormat.TRANSLUCENT
    // Position layout using gravity
    layoutParams.gravity = gravity
    return layoutParams
}

fun setRecursiveTouchListener(viewGroup: ViewGroup, listener: View.OnTouchListener) {
    viewGroup.setOnTouchListener(listener)
    for(i in 0 until viewGroup.childCount - 1) {
        val child = viewGroup.getChildAt(i)
        if(child is ViewGroup) {
            setRecursiveTouchListener(child, listener)
        } else {
            child.setOnTouchListener(listener)
        }
    }
}

fun getCoordinates(view: View, useCenter: Boolean = false): FloatArray {
    val position = intArrayOf(0, 0)
    view.getLocationOnScreen(position)
    if(useCenter) {
        position[0] += view.width / 2
        position[1] += view.height / 2
    }
    return floatArrayOf(position[0].toFloat(), position[1].toFloat())
}

fun toPixels(dp: Int): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}

fun toDP(pixels: Int): Int {
    return (pixels / Resources.getSystem().displayMetrics.density).toInt()
}