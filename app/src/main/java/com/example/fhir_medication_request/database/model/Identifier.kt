package com.example.fhir_medication_request.database.model

import kotlinx.serialization.Serializable

@Serializable
data class Identifier(
    var type: Type = Type(""),
    var value: String = ""
)