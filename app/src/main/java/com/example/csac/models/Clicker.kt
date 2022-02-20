package com.example.csac.models

import android.os.Parcel
import android.os.Parcelable
import android.view.WindowManager

class Clicker(var x: Float, var y: Float, var detectors: Array<Detector>) : Parcelable {

    constructor(parcel: Parcel) : this(
        x = parcel.readFloat(),
        y = parcel.readFloat(),
        detectors = parcel.createTypedArray(Detector.CREATOR) as Array<Detector>
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(x)
        parcel.writeFloat(y)
        parcel.writeTypedArray(detectors, 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Clicker> {
        override fun createFromParcel(parcel: Parcel): Clicker {
            return Clicker(parcel)
        }

        override fun newArray(size: Int): Array<Clicker?> {
            return arrayOfNulls(size)
        }
    }
}