package com.example.fhir_medication_request.database.model

import com.example.fhir_medication_request.common.TimestampSerializer
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

@Serializable
data class MedicationRequest(
    var id: String = "",                                            // 1..1

    @set:PropertyName("identifier")
    @get:PropertyName("identifier")
    var identifiers: MutableList<User> = mutableListOf(),           // 0..*

    var status: String = "",                                        // 1..1

    @set:PropertyName("statusReason")
    @get:PropertyName("statusReason")
    var statusReason: String? = null,                                 // 0..1

    var intent: String = "",                                        // 1..1

    @set:PropertyName("category")
    @get:PropertyName("category")
    var categories: MutableList<Category> = mutableListOf(),        // 0..*

    var priority: String? = null,                                     // 0..1
    var doNotPerform: Boolean? = null,                              // 0..1
    var reported: Boolean? = null,                                  // 0..1
    var medication: String = "",                                    // 1..1
    var subject: String = "",                                       // 1..1

    @Serializable(with = TimestampSerializer::class)
    var authoredOn: Timestamp? = null,                              // 0..1

    @set:PropertyName("note")
    @get:PropertyName("note")
    var notes: MutableList<Note> = mutableListOf(),                 // 0..*

    @set:PropertyName("dosageInstruction")
    @get:PropertyName("dosageInstruction")
    var dosageInstructions: MutableList<Dosage> = mutableListOf(),  // 0..*

    var substitution: Substitution? = null                          // 0..1
)