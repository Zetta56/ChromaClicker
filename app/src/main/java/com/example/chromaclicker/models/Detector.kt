package com.example.chromaclicker.models

import android.os.Parcelable
import com.example.chromaclicker.overlay.Line
import kotlinx.parcelize.Parcelize

@Parcelize
class Detector(var x: Float, var y: Float, var color: String) : Parcelable {
    constructor(view: Line) : this(
        x = view.endX,
        y = view.endY,
        color = view.color
    )
}