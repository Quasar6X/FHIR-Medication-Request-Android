package com.example.fhir_medication_request.database.model

import kotlinx.serialization.Serializable

@Serializable
data class Substitution(
    var allowed: Boolean = false,
    var reason: String? = ""
)