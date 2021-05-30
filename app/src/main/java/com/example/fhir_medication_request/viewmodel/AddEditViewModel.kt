package com.example.fhir_medication_request.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class AddEditViewModel : ViewModel() {
    private val _medication = MutableStateFlow("")
    private val _subject = MutableStateFlow("")

    private val _medicationError by lazy { MutableLiveData<String?>() }
    private val _subjectError by lazy { MutableLiveData<String?>() }

    val medicationError: LiveData<String?> get() = _medicationError
    val subjectError: LiveData<String?> get() = _subjectError

    var medication = ""
        set(value) {
            field = value
            _medication.value = value
            if (value.isBlank()) {
                _medicationError.value = "Must be filled"
            } else _medicationError.value = null
        }

    var subject = ""
        set(value) {
            field = value
            _subject.value = value
            if (value.isBlank()) {
                _subjectError.value = "Must be filled"
            } else _subjectError.value = null
        }

    val isSubmitEnabled: Flow<Boolean> = combine(_medication, _subject) { medication, subject ->
        return@combine medication.isNotBlank() && subject.isNotBlank()
    }
}