package com.example.csac.models

import android.os.Parcel
import android.os.Parcelable

class DetectorParcel(val x: Float, val y: Float, val color: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        x = parcel.readFloat(),
        y = parcel.readFloat(),
        color = parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(x)
        parcel.writeFloat(y)
        parcel.writeString(color)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DetectorParcel> {
        override fun createFromParcel(parcel: Parcel): DetectorParcel {
            return DetectorParcel(parcel)
        }

        override fun newArray(size: Int): Array<DetectorParcel?> {
            return arrayOfNulls(size)
        }
    }
}