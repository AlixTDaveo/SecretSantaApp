package com.example.secretsanta.ui.feature.secretsanta.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.domain.model.SecretSanta
import com.example.secretsanta.domain.repository.SecretSantaRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SecretSantaListViewModel @Inject constructor(
    private val secretSantaRepository: SecretSantaRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(SecretSantaListState())
    val state = _state.asStateFlow()

    init {
        val currentUserId = firebaseAuth.currentUser?.uid ?: ""
        val currentEmail = firebaseAuth.currentUser?.email ?: ""

        if (currentUserId.isNotEmpty()) {
            loadAllSecretSantas(currentUserId, currentEmail)
        }
    }

    private fun loadAllSecretSantas(userId: String, userEmail: String) {
        secretSantaRepository.getAllSecretSantasForUser(userId, userEmail)
            .onEach { secretSantas ->
                _state.value = _state.value.copy(
                    secretSantas = secretSantas,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }
}

data class SecretSantaListState(
    val secretSantas: List<SecretSanta> = emptyList(),
    val isLoading: Boolean = true
)