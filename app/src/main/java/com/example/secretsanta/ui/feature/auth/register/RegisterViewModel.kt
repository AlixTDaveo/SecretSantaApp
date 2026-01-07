package com.example.secretsanta.ui.feature.auth.register

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
                _state.value = _state.value.copy(email = event.email)
            }
            is RegisterEvent.PasswordChanged -> {
                _state.value = _state.value.copy(password = event.password)
            }
            is RegisterEvent.DisplayNameChanged -> {
                _state.value = _state.value.copy(displayName = event.displayName)
            }
            is RegisterEvent.Register -> {
                register()
            }
        }
    }

    private fun register() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = registerUseCase(
                email = _state.value.email,
                password = _state.value.password,
                displayName = _state.value.displayName
            )

            when (result) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    // Already handled
                }
            }
        }
    }
}

data class RegisterState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

sealed class RegisterEvent {
    data class EmailChanged(val email: String) : RegisterEvent()
    data class PasswordChanged(val password: String) : RegisterEvent()
    data class DisplayNameChanged(val displayName: String) : RegisterEvent()
    object Register : RegisterEvent()
}