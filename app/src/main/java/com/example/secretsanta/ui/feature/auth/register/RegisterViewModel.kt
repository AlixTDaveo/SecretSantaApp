package com.example.secretsanta.ui.feature.auth.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.EmailChanged -> {
                _state.value = _state.value.copy(email = event.email, error = null)
            }
            is RegisterEvent.PasswordChanged -> {
                _state.value = _state.value.copy(password = event.password, passwordError = null)
            }
            is RegisterEvent.ConfirmPasswordChanged -> {
                _state.value = _state.value.copy(confirmPassword = event.password, passwordError = null)
            }
            is RegisterEvent.DisplayNameChanged -> {
                _state.value = _state.value.copy(displayName = event.displayName, error = null)
            }
            is RegisterEvent.Register -> {
                register()
            }
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.length < 8 -> "password_too_short"
            !password.any { it.isUpperCase() } -> "password_no_uppercase"
            !password.any { it.isLowerCase() } -> "password_no_lowercase"
            !password.any { it.isDigit() } -> "password_no_digit"
            !password.any { !it.isLetterOrDigit() } -> "password_no_special"
            else -> null
        }
    }

    private fun register() {
        viewModelScope.launch {
            try {
                // Validation du mot de passe
                val passwordError = validatePassword(_state.value.password)
                if (passwordError != null) {
                    _state.value = _state.value.copy(passwordError = passwordError)
                    return@launch
                }

                // Vérification de la confirmation
                if (_state.value.password != _state.value.confirmPassword) {
                    _state.value = _state.value.copy(passwordError = "passwords_dont_match")
                    return@launch
                }

                _state.value = _state.value.copy(isLoading = true, error = null, passwordError = null)

                Log.d("RegisterViewModel", "Attempting registration for: ${_state.value.email}")

                val result = registerUseCase(
                    email = _state.value.email.trim(),
                    password = _state.value.password,
                    displayName = _state.value.displayName.trim()
                )

                when (result) {
                    is Resource.Success -> {
                        Log.d("RegisterViewModel", "Registration successful")
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                    is Resource.Error -> {
                        Log.e("RegisterViewModel", "Registration error: ${result.message}")
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message ?: "Unknown error"
                        )
                    }
                    is Resource.Loading -> {
                        // Already handled
                    }
                }
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Registration exception", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }
}

data class RegisterState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "", // NOUVEAU
    val displayName: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val passwordError: String? = null // NOUVEAU
)

sealed class RegisterEvent {
    data class EmailChanged(val email: String) : RegisterEvent()
    data class PasswordChanged(val password: String) : RegisterEvent()
    data class ConfirmPasswordChanged(val password: String) : RegisterEvent() // NOUVEAU
    data class DisplayNameChanged(val displayName: String) : RegisterEvent()
    object Register : RegisterEvent()
}