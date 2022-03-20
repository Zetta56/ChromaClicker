package com.example.csac.models

import kotlinx.serialization.Serializable

// Saves can be stored and transmitted as serializable objects
@Serializable
data class Save (
    var name: String = "",
    var clickers: List<SerializableClicker> = listOf()
) {
    // Serialize arraylist of parcelable clickers
    constructor(name: String, clickers: ArrayList<Clicker>) : this(name, clickers.map { c ->
        SerializableClicker(c.x, c.y, c.detectors.map { d -> SerializableDetector(d.x, d.y, d.color) })
    })
}

@Serializable
data class SerializableClicker(
    val x: Float,
    val y: Float,
    val detectors: List<SerializableDetector>
)

@Serializable
data class SerializableDetector(
    val x: Float,
    val y: Float,
    val color: String
)