package com.chromaclicker.app.models

import android.content.Context
import android.os.Parcelable
import com.chromaclicker.app.getDefaultPreferences
import kotlinx.parcelize.Parcelize

/**
 * A parcelable object storing app settings. You can either pass in each individual setting
 * or a context object. The latter option will populate this using your shared preferences.
 */
@Parcelize
class AppSettings(
    var random: Boolean = false,
    var clickInterval: Int = 1000,
    var detectInterval: Int = 5000,
    var circleRadius: Int = 30 // This is in DP
) : Parcelable {

    constructor(context: Context) : this() {
        val preferences = getDefaultPreferences(context)
        random = preferences.getBoolean("setting_random", false)
        clickInterval = preferences.getInt("setting_click_interval", 1000)
        detectInterval = preferences.getInt("setting_detect_interval", 5000)
        circleRadius = preferences.getInt("setting_circle_radius", 30)
    }
}
