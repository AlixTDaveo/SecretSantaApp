package com.example.secretsanta.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileState(
    val displayName: String = "",
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

sealed class EditProfileEvent {
    data class DisplayNameChanged(val v: String) : EditProfileEvent()
    data class OldPasswordChanged(val v: String) : EditProfileEvent()
    data class NewPasswordChanged(val v: String) : EditProfileEvent()
    data class ConfirmNewPasswordChanged(val v: String) : EditProfileEvent()
    object Save : EditProfileEvent()
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state = _state.asStateFlow()

    init {
        // Pré-remplir le displayName depuis Room
        authRepository.getCurrentUser()
            .onEach { user ->
                if (user != null && _state.value.displayName.isBlank()) {
                    _state.update { it.copy(displayName = user.displayName) }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: EditProfileEvent) {
        when (event) {
            is EditProfileEvent.DisplayNameChanged -> _state.update { it.copy(displayName = event.v, error = null) }
            is EditProfileEvent.OldPasswordChanged -> _state.update { it.copy(oldPassword = event.v, error = null) }
            is EditProfileEvent.NewPasswordChanged -> _state.update { it.copy(newPassword = event.v, error = null) }
            is EditProfileEvent.ConfirmNewPasswordChanged -> _state.update { it.copy(confirmNewPassword = event.v, error = null) }
            EditProfileEvent.Save -> save()
        }
    }

    private fun save() {
        val s = _state.value

        // Validation: pro, mais simple
        if (s.displayName.isBlank()) {
            _state.update { it.copy(error = "Le nom affiché ne peut pas être vide.") }
            return
        }

        val wantsPasswordChange =
            s.oldPassword.isNotBlank() || s.newPassword.isNotBlank() || s.confirmNewPassword.isNotBlank()

        if (wantsPasswordChange) {
            if (s.oldPassword.isBlank()) {
                _state.update { it.copy(error = "Veuillez renseigner l'ancien mot de passe.") }
                return
            }
            if (s.newPassword.length < 8) {
                _state.update { it.copy(error = "Le nouveau mot de passe doit contenir au moins 8 caractères.") }
                return
            }
            if (s.newPassword != s.confirmNewPassword) {
                _state.update { it.copy(error = "Les nouveaux mots de passe ne correspondent pas.") }
                return
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null, success = false) }

            // 1) Update displayName
            when (val r = authRepository.updateDisplayName(s.displayName.trim())) {
                is Resource.Error -> {
                    _state.update { it.copy(isSaving = false, error = r.message) }
                    return@launch
                }
                else -> Unit
            }

            // 2) Password change (optionnel)
            if (wantsPasswordChange) {
                when (val r = authRepository.changePassword(s.oldPassword, s.newPassword)) {
                    is Resource.Error -> {
                        _state.update { it.copy(isSaving = false, error = r.message) }
                        return@launch
                    }
                    else -> Unit
                }
            }

            _state.update { it.copy(isSaving = false, success = true) }
        }
    }
}
