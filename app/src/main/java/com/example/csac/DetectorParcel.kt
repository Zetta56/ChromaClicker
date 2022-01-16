package com.example.csac

import android.os.Parcel
import android.os.Parcelable

class DetectorParcel(val x: Int, val y: Int, val color: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        x = parcel.readInt(),
        y = parcel.readInt(),
        color = parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(x)
        parcel.writeInt(y)
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