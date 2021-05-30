package com.example.fhir_medication_request.common

import android.util.Log
import com.google.firebase.Timestamp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

object TimestampSerializer : KSerializer<Timestamp> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("com.google.firebase.Timestamp") {
            element<Long>("seconds")
            element<Int>("nanoseconds")
        }

    override fun serialize(encoder: Encoder, value: Timestamp) = encoder.encodeStructure(descriptor) {
        encodeLongElement(descriptor, 0, value.seconds)
        encodeIntElement(descriptor, 1, value.nanoseconds)
    }

    override fun deserialize(decoder: Decoder): Timestamp = decoder.decodeStructure(descriptor) {
        var seconds = -1L
        var nanos = -1
        loop@while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> seconds = decodeLongElement(descriptor, 0)
                1 -> nanos = decodeIntElement(descriptor, 1)
                CompositeDecoder.DECODE_DONE -> break@loop
                else -> { Log.w(TimestampSerializer::class.java.simpleName, "Unexpected index: $index"); break@loop }
            }
        }
        require(seconds >= 0 && nanos in 0..999_999_999)
        Timestamp(seconds, nanos)
    }
}