package com.example.fhir_medication_request.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

private const val usernameRegex = "^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){3,18}[a-zA-Z0-9]\$" // 5-20 karakter, nem kezdodik vagy vegzodik ezekkel: . _ -
private const val emailRegex = "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])"

class RegistrationViewModel : ViewModel() {
    private val _username = MutableStateFlow("")
    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _passwordConfirm = MutableStateFlow("")

    private val _usernameError: MutableLiveData<String?> by lazy { MutableLiveData<String?>() }
    private val _emailError: MutableLiveData<String?> by lazy { MutableLiveData<String?>() }
    private val _passwordError: MutableLiveData<String?> by lazy { MutableLiveData<String?>() }
    private val _passwordConfirmError: MutableLiveData<String?> by lazy { MutableLiveData<String?>() }

    val usernameError: LiveData<String?> get() = _usernameError
    val emailError: LiveData<String?> get() = _emailError
    val passwordError: LiveData<String?> get() = _passwordError
    val passwordConfirmError: LiveData<String?> get() = _passwordConfirmError

    var username = ""
        set(value) {
            field = value
            _username.value = value
            if (!isUsernameCorrect(value)) {
                _usernameError.value = "Username must be between 5-20 and does not start or end with a (.), (_), or (-)"
            } else _usernameError.value = null
        }

    var email = ""
        set(value) {
            field = value
            _email.value = value
            if (!isEmailCorrect(value)) {
                _emailError.value = "Invalid e-mail address"
            } else _emailError.value = null
        }

    var password = ""
        set(value) {
            field = value
            _password.value = value
            if (!isPasswordCorrect(value)) {
                _passwordError.value = "Password must be at least 6 characters"
            } else _passwordError.value = null
        }

    var passwordConfirm = ""
        set(value) {
            field = value
            _passwordConfirm.value = value
            if (!isPasswordCorrect(value)) {
                _passwordConfirmError.value = "Password must be at least 6 characters"
            } else _passwordConfirmError.value = null
        }

    val isSubmitEnabled: Flow<Boolean> = combine(_username, _email, _password, _passwordConfirm) { username, email, password, passwordConfirm ->
        return@combine isUsernameCorrect(username) &&
                isEmailCorrect(email) &&
                isPasswordCorrect(password) &&
                isPasswordCorrect(passwordConfirm)
    }

    private fun isUsernameCorrect(username: String)
        = username.matches(usernameRegex.toRegex())

    private fun isEmailCorrect(email: String)
        = email.matches(emailRegex.toRegex())

    private fun isPasswordCorrect(password: String)
        = password.length > 5
}