package com.example.fhir_medication_request.database.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    var uid: Identifier = Identifier(Type("uid"), ""),
    var email: Identifier = Identifier(Type("email"), ""),
    var displayName: Identifier = Identifier(Type("displayName"), "")
)