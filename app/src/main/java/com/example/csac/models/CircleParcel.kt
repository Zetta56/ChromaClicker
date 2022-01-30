package com.example.csac.models

import android.os.Parcel
import android.os.Parcelable
import com.example.csac.getCoordinates
import com.example.csac.overlay.CircleView

class CircleParcel(var x: Float, var y: Float, var detectors: Array<DetectorParcel>) : Parcelable {

    constructor() : this(0f, 0f, arrayOf<DetectorParcel>())

    constructor(parcel: Parcel) : this(
        x = parcel.readFloat(),
        y = parcel.readFloat(),
        detectors = arrayOf<DetectorParcel>().apply {
            parcel.createTypedArray(DetectorParcel)
        }
    )

    constructor(view: CircleView) : this() {
        val position = getCoordinates(view)
        x = position[0] + (view.width / 2)
        y = position[1] + (view.height / 2)
        detectors = arrayOf()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(x)
        parcel.writeFloat(y)
        parcel.writeTypedArray(detectors, 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CircleParcel> {
        override fun createFromParcel(parcel: Parcel): CircleParcel {
            return CircleParcel(parcel)
        }

        override fun newArray(size: Int): Array<CircleParcel?> {
            return arrayOfNulls(size)
        }
    }
}