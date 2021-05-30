package com.example.fhir_medication_request.database.repository

import com.example.fhir_medication_request.database.model.MedicationRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val COLLECTION_NAME = "MedicationRequests"

class MedicationRequestRepository(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val remote: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getMedicationRequests(): Result<List<MedicationRequest>> = withContext(dispatcher) {
        try {
            val res = remote.collection(COLLECTION_NAME).get().await()
            val list: MutableList<MedicationRequest> = mutableListOf()
            res.forEach { document ->
                val request = document.toObject(MedicationRequest::class.java)
                request.id = document.id
                list.add(request)
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insert(mr: MedicationRequest) = withContext(dispatcher) {
        remote.collection(COLLECTION_NAME).add(mr).addOnSuccessListener {
            remote.collection(COLLECTION_NAME).document(it.id).update("id", it.id)
        }
    }

    suspend fun update(mr: MedicationRequest) = withContext(dispatcher) {
        val docRef = remote.collection(COLLECTION_NAME).document(mr.id)

        remote.runTransaction { transaction ->
            transaction.update(
                docRef, mapOf(
                    "status" to mr.status,
                    "statusReason" to mr.statusReason,
                    "intent" to mr.intent,
                    "priority" to mr.priority,
                    "doNotPerform" to mr.doNotPerform,
                    "reported" to mr.reported,
                    "medication" to mr.medication,
                    "subject" to mr.subject,
                    "authoredOn" to mr.authoredOn,
                    "substitution.allowed" to mr.substitution?.allowed,
                    "substitution.reason" to mr.substitution?.reason
                )
            )
            transaction.update(
                docRef,
                "identifier",
                FieldValue.arrayUnion(*mr.identifiers.toTypedArray())
            )
            transaction.update(
                docRef,
                "category",
                FieldValue.arrayUnion(*mr.categories.toTypedArray())
            )
            transaction.update(docRef, "note", FieldValue.arrayUnion(*mr.notes.toTypedArray()))
            transaction.update(
                docRef,
                "dosageInstruction",
                FieldValue.arrayUnion(*mr.dosageInstructions.toTypedArray())
            )
            null
        }
    }

    suspend fun delete(mr: MedicationRequest) = withContext(dispatcher) {
        remote.collection(COLLECTION_NAME).document(mr.id).delete()
    }
}