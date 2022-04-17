package com.example.csac.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Save(
    var name: String = "",
    var clickers: List<Clicker> = listOf()
) : Parcelable