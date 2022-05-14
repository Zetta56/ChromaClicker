package com.example.chromaclicker.models

import android.os.Parcelable
import com.example.chromaclicker.overlay.DetectorView
import kotlinx.parcelize.Parcelize

@Parcelize
class Detector(var x: Float, var y: Float, var color: String) : Parcelable {
    constructor(view: DetectorView) : this(
        x = view.endX,
        y = view.endY,
        color = view.color
    )
}