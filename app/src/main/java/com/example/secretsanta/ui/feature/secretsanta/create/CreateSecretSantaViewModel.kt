package com.example.secretsanta.ui.feature.secretsanta.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.domain.model.Participant
import com.example.secretsanta.domain.model.SecretSanta
import com.example.secretsanta.domain.usecase.secretsanta.CreateSecretSantaUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CreateSecretSantaViewModel @Inject constructor(
    private val createSecretSantaUseCase: CreateSecretSantaUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(CreateSecretSantaState())
    val state = _state.asStateFlow()

    fun onEvent(event: CreateSecretSantaEvent) {
        when (event) {
            is CreateSecretSantaEvent.NameChanged -> {
                _state.value = _state.value.copy(name = event.name, error = null)
            }
            is CreateSecretSantaEvent.DeadlineChanged -> {
                _state.value = _state.value.copy(deadline = event.deadline, error = null)
            }
            is CreateSecretSantaEvent.BudgetChanged -> {
                _state.value = _state.value.copy(budget = event.budget, error = null)
            }
            is CreateSecretSantaEvent.DescriptionChanged -> {
                _state.value = _state.value.copy(description = event.description, error = null)
            }
            is CreateSecretSantaEvent.ParticipantNameChanged -> {
                _state.value = _state.value.copy(
                    currentParticipantName = event.name,
                    error = null
                )
            }
            is CreateSecretSantaEvent.ParticipantEmailChanged -> {
                _state.value = _state.value.copy(
                    currentParticipantEmail = event.email,
                    error = null
                )
            }
            is CreateSecretSantaEvent.AddParticipant -> {
                addParticipant()
            }
            is CreateSecretSantaEvent.RemoveParticipant -> {
                removeParticipant(event.participantId)
            }
            is CreateSecretSantaEvent.CreateSecretSanta -> {
                createSecretSanta()
            }
        }
    }

    private fun addParticipant() {
        val name = _state.value.currentParticipantName.trim()
        val email = _state.value.currentParticipantEmail.trim()

        when {
            name.isEmpty() -> {
                _state.value = _state.value.copy(error = "Le nom est requis")
                return
            }
            email.isEmpty() -> {
                _state.value = _state.value.copy(error = "L'email est requis")
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _state.value = _state.value.copy(error = "Email invalide")
                return
            }
            _state.value.participants.any { it.email.equals(email, ignoreCase = true) } -> {
                _state.value = _state.value.copy(error = "Cet email est déjà utilisé")
                return
            }
        }

        val participant = Participant(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email
        )

        _state.value = _state.value.copy(
            participants = _state.value.participants + participant,
            currentParticipantName = "",
            currentParticipantEmail = "",
            error = null
        )
    }

    private fun removeParticipant(participantId: String) {
        _state.value = _state.value.copy(
            participants = _state.value.participants.filter { it.id != participantId }
        )
    }

    private fun createSecretSanta() {
        viewModelScope.launch {
            try {
                if (_state.value.name.trim().isEmpty()) {
                    _state.value = _state.value.copy(error = "Le nom du groupe est requis")
                    return@launch
                }

                // 🔽 MODIFICATION B1 : Minimum 3 participants
                if (_state.value.participants.size < 3) {
                    _state.value = _state.value.copy(
                        error = "❌ Minimum 3 participants requis pour un Secret Santa"
                    )
                    return@launch
                }
                // 🔼 FIN MODIFICATION B1

                if (_state.value.deadline == 0L) {
                    _state.value = _state.value.copy(error = "Sélectionnez une date limite")
                    return@launch
                }

                _state.value = _state.value.copy(isLoading = true, error = null)

                val currentUserId = firebaseAuth.currentUser?.uid ?: ""
                val currentUserEmail = firebaseAuth.currentUser?.email ?: ""

                val participantsWithOrganizer = _state.value.participants.map { participant ->
                    if (participant.email.equals(currentUserEmail, ignoreCase = true)) {
                        participant.copy(isOrganizer = true, userId = currentUserId)
                    } else {
                        participant
                    }
                }

                val secretSanta = SecretSanta(
                    id = UUID.randomUUID().toString(),
                    name = _state.value.name.trim(),
                    deadline = _state.value.deadline,
                    participants = participantsWithOrganizer,
                    creatorId = currentUserId,
                    drawDone = false,
                    assignments = emptyMap(),
                    budget = _state.value.budget.trim().ifEmpty { null },
                    description = _state.value.description.trim().ifEmpty { null }
                )

                Log.d("CreateVM", "Creating Secret Santa: ${secretSanta.name}")

                val result = createSecretSantaUseCase(secretSanta)

                when (result) {
                    is Resource.Success -> {
                        Log.d("CreateVM", "Secret Santa created successfully")
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                    is Resource.Error -> {
                        Log.e("CreateVM", "Error: ${result.message}")
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message ?: "Erreur de création"
                        )
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                Log.e("CreateVM", "Exception", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Une erreur est survenue"
                )
            }
        }
    }
}

data class CreateSecretSantaState(
    val name: String = "",
    val deadline: Long = 0L,
    val budget: String = "",
    val description: String = "",
    val participants: List<Participant> = emptyList(),
    val currentParticipantName: String = "",
    val currentParticipantEmail: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

sealed class CreateSecretSantaEvent {
    data class NameChanged(val name: String) : CreateSecretSantaEvent()
    data class DeadlineChanged(val deadline: Long) : CreateSecretSantaEvent()
    data class BudgetChanged(val budget: String) : CreateSecretSantaEvent()
    data class DescriptionChanged(val description: String) : CreateSecretSantaEvent()
    data class ParticipantNameChanged(val name: String) : CreateSecretSantaEvent()
    data class ParticipantEmailChanged(val email: String) : CreateSecretSantaEvent()
    object AddParticipant : CreateSecretSantaEvent()
    data class RemoveParticipant(val participantId: String) : CreateSecretSantaEvent()
    object CreateSecretSanta : CreateSecretSantaEvent()
}