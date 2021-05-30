package com.example.fhir_medication_request.common

import android.content.Context
import android.content.Intent
import com.example.fhir_medication_request.MainActivity

internal const val SECRET_KEY = "com.example.fhir_medication_request.common.UID"
internal const val SNACK = "com.example.fhir_medication_request.common.snack"
internal const val GUEST = "com.example.fhir_medication_request.common.guest"
internal const val MEDICATION_REQUEST = "com.example.fhir_medication_request.common.medication_request"

internal fun goToMain(context: Context, uid: String, guest: Boolean): Intent =
    Intent(context, MainActivity::class.java)
        .putExtra(SECRET_KEY, uid)
        .putExtra(GUEST, guest)
