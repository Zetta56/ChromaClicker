package com.example.chromaclicker

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

fun createOverlayLayout(
    width: Int = getScreenWidth("dp"),
    height: Int = getScreenHeight("dp"),
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
    for(i in 0 until viewGroup.childCount) {
        val child = viewGroup.getChildAt(i)
        if(child is ViewGroup) {
            setRecursiveTouchListener(child, listener)
        } else {
            child.setOnTouchListener(listener)
        }
    }
}

fun getDefaultPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(context.getString(R.string.preference_key), Context.MODE_PRIVATE)
}

fun toPixels(dp: Int): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}

fun toDP(pixels: Int): Int {
    return (pixels / Resources.getSystem().displayMetrics.density).toInt()
}

fun getScreenWidth(unit: String = "px"): Int {
    return when(unit) {
        "dp" -> toDP(Resources.getSystem().displayMetrics.widthPixels)
        else -> Resources.getSystem().displayMetrics.widthPixels
    }
}

fun getScreenHeight(unit: String = "px"): Int {
    return when(unit) {
        "dp" -> toDP(Resources.getSystem().displayMetrics.heightPixels)
        else -> Resources.getSystem().displayMetrics.heightPixels
    }
}