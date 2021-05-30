package com.example.fhir_medication_request.database.model

import com.example.fhir_medication_request.common.TimestampSerializer
import com.google.firebase.Timestamp
import kotlinx.serialization.Serializable

@Serializable
data class Note(
    var author: Author? = null,
    @Serializable(with = TimestampSerializer::class)
    var time: Timestamp? = null,
    var text: String = ""
)