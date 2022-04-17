package com.example.csac.models

import android.content.Context
import android.os.Parcelable
import com.example.csac.getDefaultPreferences
import kotlinx.parcelize.Parcelize

@Parcelize
class AppSettings(
    var random: Boolean = false
) : Parcelable {
    constructor(context: Context) : this() {
        val preferences = getDefaultPreferences(context)
        random = preferences.getBoolean("setting_random", false)
    }
}
