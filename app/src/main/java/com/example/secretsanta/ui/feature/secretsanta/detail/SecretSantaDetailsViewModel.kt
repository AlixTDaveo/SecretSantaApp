package com.example.secretsanta.ui.feature.secretsanta.details

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.domain.model.SecretSanta
import com.example.secretsanta.domain.usecase.secretsanta.DeleteSecretSantaUseCase
import com.example.secretsanta.domain.usecase.secretsanta.GetSecretSantaByIdUseCase
import com.example.secretsanta.domain.usecase.secretsanta.PerformDrawUseCase
import com.example.secretsanta.domain.repository.SecretSantaRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecretSantaDetailsViewModel @Inject constructor(
    private val getSecretSantaByIdUseCase: GetSecretSantaByIdUseCase,
    private val performDrawUseCase: PerformDrawUseCase,
    private val deleteSecretSantaUseCase: DeleteSecretSantaUseCase,
    private val secretSantaRepository: SecretSantaRepository,
    private val firebaseAuth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val santaId: String = savedStateHandle.get<String>("santaId") ?: ""

    private val _state = MutableStateFlow(SecretSantaDetailsState())
    val state = _state.asStateFlow()

    init {
        loadSecretSanta()
    }

    private fun loadSecretSanta() {
        viewModelScope.launch {
            getSecretSantaByIdUseCase(santaId).collect { secretSanta ->
                if (secretSanta != null) {
                    val currentUserId = firebaseAuth.currentUser?.uid ?: ""
                    val isOrganizer = secretSanta.creatorId == currentUserId

                    _state.value = _state.value.copy(
                        secretSanta = secretSanta,
                        isLoading = false,
                        isOrganizer = isOrganizer
                    )
                }
            }
        }
    }

    fun onEvent(event: SecretSantaDetailsEvent) {
        when (event) {
            is SecretSantaDetailsEvent.PerformDraw -> performDraw()
            is SecretSantaDetailsEvent.DeleteSecretSanta -> deleteSecretSanta()
            is SecretSantaDetailsEvent.RemoveParticipant -> removeParticipant(event.participantId)
            is SecretSantaDetailsEvent.DismissError -> {
                _state.value = _state.value.copy(error = null)
            }
            is SecretSantaDetailsEvent.ShowError -> {
                _state.value = _state.value.copy(error = event.message)
            }
        }
    }

    private fun performDraw() {
        viewModelScope.launch {
            try {
                val secretSanta = _state.value.secretSanta ?: return@launch
                _state.value = _state.value.copy(isPerformingDraw = true, error = null)

                val result = performDrawUseCase(secretSanta)

                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isPerformingDraw = false,
                            secretSanta = result.data
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isPerformingDraw = false,
                            error = result.message
                        )
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isPerformingDraw = false,
                    error = e.message
                )
            }
        }
    }

    private fun deleteSecretSanta() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isDeleting = true, error = null)

                val result = deleteSecretSantaUseCase(santaId)

                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isDeleting = false,
                            isDeleted = true
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isDeleting = false,
                            error = result.message
                        )
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isDeleting = false,
                    error = e.message
                )
            }
        }
    }

    private fun removeParticipant(participantId: String) {
        viewModelScope.launch {
            try {
                val result = secretSantaRepository.removeParticipant(santaId, participantId)

                when (result) {
                    is Resource.Success -> {
                        Log.d("DetailsVM", "Participant removed successfully")
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(error = result.message)
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}

data class SecretSantaDetailsState(
    val secretSanta: SecretSanta? = null,
    val isLoading: Boolean = true,
    val isPerformingDraw: Boolean = false,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val isOrganizer: Boolean = false,
    val error: String? = null
)

sealed class SecretSantaDetailsEvent {
    object PerformDraw : SecretSantaDetailsEvent()
    object DeleteSecretSanta : SecretSantaDetailsEvent()
    data class RemoveParticipant(val participantId: String) : SecretSantaDetailsEvent()
    object DismissError : SecretSantaDetailsEvent()
    data class ShowError(val message: String) : SecretSantaDetailsEvent()
}