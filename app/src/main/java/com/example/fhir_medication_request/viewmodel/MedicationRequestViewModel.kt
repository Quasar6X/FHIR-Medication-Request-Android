package com.example.fhir_medication_request.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.fhir_medication_request.database.model.MedicationRequest
import com.example.fhir_medication_request.database.repository.MedicationRequestRepository
import kotlinx.coroutines.launch

class MedicationRequestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MedicationRequestRepository = MedicationRequestRepository()

    fun getMedicationRequests(): LiveData<List<MedicationRequest>> =
        liveData {
            val result = try {
                repository.getMedicationRequests()
            } catch (e: Exception) {
                Result.failure(e)
            }
            if (result.isSuccess)
                emit(result.getOrThrow())
            else
                Toast.makeText(getApplication(), "Could not get medication requests", Toast.LENGTH_LONG).show()
        }

    fun insert(medicationRequest: MedicationRequest) =
        viewModelScope.launch {
            repository.insert(medicationRequest)
        }

    fun update(medicationRequest: MedicationRequest) =
        viewModelScope.launch {
            repository.update(medicationRequest)
        }

    fun delete(medicationRequest: MedicationRequest) =
        viewModelScope.launch {
            repository.delete(medicationRequest)
        }
}