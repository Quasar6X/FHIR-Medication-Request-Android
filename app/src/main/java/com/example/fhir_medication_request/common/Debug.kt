package com.example.fhir_medication_request.common

import android.util.Log
import com.example.fhir_medication_request.database.model.MedicationRequest

@Suppress("UNUSED")
internal fun logMedicationRequest(logTag: String?, request: MedicationRequest) {
    Log.d(logTag, "-------------------REQ START-------------------")

    Log.d(logTag, "ID: ${request.id}")
    request.identifiers.forEach { user ->
        Log.d(logTag, "Identifier DISPLAYNAME: ${user.uid.value}")
        Log.d(logTag, "Identifier DISPLAYNAME: ${user.email.value}")
        Log.d(logTag, "Identifier DISPLAYNAME: ${user.displayName.value}")
    }
    Log.d(logTag, "Status: ${request.status}")
    request.statusReason?.let { Log.d(logTag, "Status reason: $it") }
    Log.d(logTag, "Intent: ${request.intent}")
    request.categories.forEach { category ->
        Log.d(logTag, "Category text: ${category.text}")
    }
    request.priority?.let { Log.d(logTag, "Priority: $it") }
    request.doNotPerform?.let { Log.d(logTag, "Do not perform: $it") }
    request.reported?.let { Log.d(logTag, "Reported: $it") }
    Log.d(logTag, "Medication: ${request.medication}")
    Log.d(logTag, "Subject: ${request.subject}")
    request.authoredOn?.let { Log.d(logTag, "Authored on: ${it.toDate()}") }
    request.notes.forEach { note ->
        note.author?.authorString?.let { Log.d(logTag, "Note authorString: $it") }
        Log.d(logTag, "Note time: ${note.time?.toDate()}")
        Log.d(logTag, "Note text: ${note.text}")
    }
    request.dosageInstructions.forEach { dosage ->
        Log.d(logTag, "Dosage text: ${dosage.text}")
    }
    request.substitution?.let { substitution ->
        Log.d(logTag, "Substitution allowed: ${substitution.allowed}")
        substitution.reason?.let { Log.d(logTag, "Substitution reason: $it") }
    }
    Log.d(logTag, "-------------------REQ END-------------------")
}