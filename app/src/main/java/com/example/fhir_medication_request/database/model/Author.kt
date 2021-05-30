package com.example.fhir_medication_request.database.model

import kotlinx.serialization.Serializable

@Serializable
data class Author(
    var authorString: String? = null
)