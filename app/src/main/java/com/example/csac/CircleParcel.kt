package com.example.csac

import android.os.Parcel
import android.os.Parcelable

class CircleParcel(var x: Int, var y: Int, var detectors: Array<DetectorParcel>) : Parcelable {
    constructor() : this(0, 0, arrayOf<DetectorParcel>())

    constructor(parcel: Parcel) : this(
        x = parcel.readInt(),
        y = parcel.readInt(),
        detectors = arrayOf<DetectorParcel>().apply {
            parcel.createTypedArray(DetectorParcel.CREATOR)
        }
    )

    constructor(view: CircleView) : this() {
        val position = intArrayOf(0, 0)
        view.getLocationOnScreen(position)
        x = position[0] + (view.width / 2)
        y = position[1] + (view.height / 2)
        detectors = arrayOf<DetectorParcel>(
            DetectorParcel(0, 0, "#EEEEEE")
        )
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(x)
        parcel.writeInt(y)
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