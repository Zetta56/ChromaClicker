package com.example.chromaclicker.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** A parcelable object storing clicker data */
@Parcelize
class Clicker(
    var x: Float,
    var y: Float,
    var detectors: Array<Detector>,
    var isClicking: Boolean = false
) : Parcelable