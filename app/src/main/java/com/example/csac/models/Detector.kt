package com.example.csac.models

import android.os.Parcel
import android.os.Parcelable
import com.example.csac.overlay.DetectorView

class Detector(var x: Float, var y: Float, var color: String) : Parcelable {
    constructor() : this(0f, 0f, "#000000")

    constructor(parcel: Parcel) : this(
        x = parcel.readFloat(),
        y = parcel.readFloat(),
        color = parcel.readString()!!
    )

    constructor(view: DetectorView) : this() {
        x = view.endX
        y = view.endY
        color = view.color
    }

    constructor(serializable: SerializableDetector) : this(
        x = serializable.x,
        y = serializable.y,
        color = serializable.color
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(x)
        parcel.writeFloat(y)
        parcel.writeString(color)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Detector> {
        override fun createFromParcel(parcel: Parcel): Detector {
            return Detector(parcel)
        }

        override fun newArray(size: Int): Array<Detector?> {
            return arrayOfNulls(size)
        }
    }
}