package com.example.secretsanta.ui.feature.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.domain.model.User
import com.example.secretsanta.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val loggedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    init {
        authRepository.getCurrentUser()
            .onEach { user ->
                _state.update { it.copy(user = user, isLoading = false, error = null) }
            }
            .catch { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "Error") }
            }
            .launchIn(viewModelScope)
    }

    fun logout() {
        Log.d("ProfileViewModel", "🔴 User clicked LOGOUT")
        viewModelScope.launch {
            Log.d("ProfileViewModel", "Calling authRepository.logout()...")
            authRepository.logout()
            Log.d("ProfileViewModel", "✅ Logout successful, updating state")
            _state.update { it.copy(loggedOut = true) }
            Log.d("ProfileViewModel", "🟢 State updated: loggedOut=true")
        }
    }
}
