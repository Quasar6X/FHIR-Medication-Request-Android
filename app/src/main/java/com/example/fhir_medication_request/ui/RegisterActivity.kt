package com.example.fhir_medication_request.ui

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.fhir_medication_request.common.goToMain
import com.example.fhir_medication_request.databinding.ActivityRegisterBinding
import com.example.fhir_medication_request.viewmodel.RegistrationViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameET: EditText
    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var passwordConfirmET: EditText
    private lateinit var binding: ActivityRegisterBinding

    private val auth by lazy { Firebase.auth }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val model: RegistrationViewModel by viewModels()
        setContentView(binding.root)

        usernameET = binding.userNameEditText.editText!!
        emailET = binding.userEmailEditText.editText!!
        passwordET = binding.passwordEditText.editText!!
        passwordConfirmET = binding.confirmPasswordEditText.editText!!

        usernameET.addTextChangedListener { model.username = it.toString() }
        emailET.addTextChangedListener { model.email = it.toString() }
        passwordET.addTextChangedListener { model.password = it.toString() }
        passwordConfirmET.addTextChangedListener { model.passwordConfirm = it.toString() }

        model.usernameError.observe(this) { binding.userNameEditText.error = it }
        model.emailError.observe(this) { binding.userEmailEditText.error = it }
        model.passwordError.observe(this) { binding.passwordEditText.error = it }
        model.passwordConfirmError.observe(this) { binding.confirmPasswordEditText.error = it }

        lifecycleScope.launch {
            model.isSubmitEnabled.collect { value ->
                binding.register.isEnabled = value
            }
        }
    }

    fun register(@Suppress("UNUSED_PARAMETER") source: View) {
        val username = usernameET.text.toString()
        val email = emailET.text.toString()
        val password = passwordET.text.toString()
        val passwordConfirm = passwordConfirmET.text.toString()

        if (password != passwordConfirm) {
//            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_LONG).show()
            Snackbar.make(source, "Passwords do not match!", Snackbar.LENGTH_LONG).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = task.result?.user ?: return@addOnCompleteListener
                user.updateProfile(userProfileChangeRequest {
                    displayName = username
                })
                startActivity(goToMain(this, user.uid, user.isAnonymous))
            } else {
//                Toast.makeText(
//                    this,
//                    "${task.exception?.message}",
//                    Toast.LENGTH_LONG
//                ).show()
                Snackbar.make(
                    source,
                    "${task.exception?.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    fun cancel(@Suppress("UNUSED_PARAMETER") source: View) = finish()
}