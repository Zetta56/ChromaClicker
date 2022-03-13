package com.example.csac.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Save (
    val name: String = "",
//    @Serializable(with = ClickerListSerializer::class)
    val clickers: List<SerializableClicker> = listOf()
) {
    constructor(name: String, clickers: ArrayList<Clicker>) : this(name, clickers.map { c ->
        SerializableClicker(c.x, c.y, c.detectors.map { d -> SerializableDetector(d.x, d.y, d.color) })
    })
}

@Serializable
data class SerializableClicker(
    val x: Float,
    val y: Float,
//    @Serializable(with = DetectorListSerializer::class)
    val detectors: List<SerializableDetector>
)

@Serializable
data class SerializableDetector(
    val x: Float,
    val y: Float,
    val color: String
)

private class ClickerListSerializer : KSerializer<List<SerializableClicker>> {
    private val builtIn: KSerializer<List<SerializableClicker>> = ListSerializer(SerializableClicker.serializer())
    override val descriptor: SerialDescriptor = builtIn.descriptor

    override fun deserialize(decoder: Decoder): List<SerializableClicker> {
        return builtIn.deserialize(decoder)
    }
    override fun serialize(encoder: Encoder, value: List<SerializableClicker>) {
        builtIn.serialize(encoder, value)
    }
}

private class DetectorListSerializer : KSerializer<List<SerializableDetector>> {
    private val builtIn: KSerializer<List<SerializableDetector>> = ListSerializer(SerializableDetector.serializer())
    override val descriptor: SerialDescriptor = builtIn.descriptor

    override fun deserialize(decoder: Decoder): List<SerializableDetector> {
        return builtIn.deserialize(decoder)
    }
    override fun serialize(encoder: Encoder, value: List<SerializableDetector>) {
        builtIn.serialize(encoder, value)
    }
}