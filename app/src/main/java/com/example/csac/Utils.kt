package com.example.csac

import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import java.io.File
import kotlin.math.max
import kotlin.math.min

fun createOverlayLayout(
    width: Int = toDP(Resources.getSystem().displayMetrics.widthPixels),
    height: Int = toDP(Resources.getSystem().displayMetrics.heightPixels),
    x: Int = 0,
    y: Int = 0,
    gravity: Int = Gravity.NO_GRAVITY,
    focusable: Boolean = false
): WindowManager.LayoutParams {
    val layoutParams = WindowManager.LayoutParams()
    // Convert width and height from dp to pixels
    layoutParams.width = toPixels(width)
    layoutParams.height = toPixels(height)
    layoutParams.x = toPixels(x)
    layoutParams.y = toPixels(y)
    // Display this on top of other applications
    layoutParams.type = if(Build.VERSION.SDK_INT >= 26) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
        @Suppress("Deprecation")
        WindowManager.LayoutParams.TYPE_PHONE
    }
    // Conditionally make user unable to give input focus to this
    if(!focusable) {
        layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
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

fun toPixels(dp: Int): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}

fun toDP(pixels: Int): Int {
    return (pixels / Resources.getSystem().displayMetrics.density).toInt()
}

fun clamp(num: Int, lower: Int, upper: Int): Int {
    return min(max(num, lower), upper)
}