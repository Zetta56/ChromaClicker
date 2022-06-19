package com.chromaclicker.app.models

import android.os.Parcelable
import com.chromaclicker.app.overlay.Line
import kotlinx.parcelize.Parcelize

/** A parcelable object storing detector data */
@Parcelize
class Detector(var x: Float, var y: Float, var color: String) : Parcelable {
    // Constructs a detector from a line view's endpoint and color
    constructor(view: Line) : this(
        x = view.endX,
        y = view.endY,
        color = view.color
    )
}