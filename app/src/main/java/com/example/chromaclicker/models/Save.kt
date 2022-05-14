package com.example.chromaclicker.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Save(
    var name: String = "",
    var clickers: List<Clicker> = listOf()
) : Parcelable