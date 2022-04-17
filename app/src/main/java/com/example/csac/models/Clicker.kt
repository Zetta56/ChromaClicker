package com.example.csac.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Clicker(var x: Float, var y: Float, var detectors: Array<Detector>) : Parcelable