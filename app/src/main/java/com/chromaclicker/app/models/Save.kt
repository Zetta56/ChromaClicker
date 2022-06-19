package com.chromaclicker.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** A parcelable object storing save data */
@Parcelize
class Save(
    var name: String = "",
    var clickers: List<Clicker> = listOf()
) : Parcelable