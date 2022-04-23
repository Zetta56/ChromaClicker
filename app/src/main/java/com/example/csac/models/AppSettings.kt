package com.example.csac.models

import android.content.Context
import android.os.Parcelable
import com.example.csac.getDefaultPreferences
import kotlinx.parcelize.Parcelize

@Parcelize
class AppSettings(
    var random: Boolean = false,
    var clickInterval: Int = 1000,
    var detectInterval: Int = 5000,
    var circleRadius: Int = 30
) : Parcelable {

    constructor(context: Context) : this() {
        val preferences = getDefaultPreferences(context)
        random = preferences.getBoolean("setting_random", false)
        clickInterval = preferences.getInt("setting_click_interval", 1000)
        detectInterval = preferences.getInt("setting_detect_interval", 5000)
        circleRadius = preferences.getInt("setting_circle_radius", 30)
    }
}
