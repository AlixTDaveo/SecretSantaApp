package com.example.secretsanta.ui.feature.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.core.util.SoundManager
import com.example.secretsanta.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val soundManager: SoundManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _state.value = _state.value.copy(email = event.email, error = null)
            }
            is LoginEvent.PasswordChanged -> {
                _state.value = _state.value.copy(password = event.password, error = null)
            }
            is LoginEvent.Login -> {
                login()
            }
        }
    }

    private fun login() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)

                Log.d("LoginViewModel", "Attempting login for: ${_state.value.email}")

                val result = loginUseCase(
                    email = _state.value.email.trim(),
                    password = _state.value.password
                )

                when (result) {
                    is Resource.Success -> {
                        Log.d("LoginViewModel", "Login successful")

                        // Joue le son de succès
                        soundManager.playSuccessSound()

                        _state.value = _state.value.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                    is Resource.Error -> {
                        Log.e("LoginViewModel", "Login error: ${result.message}")
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
                Log.e("LoginViewModel", "Login exception", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object Login : LoginEvent()
}