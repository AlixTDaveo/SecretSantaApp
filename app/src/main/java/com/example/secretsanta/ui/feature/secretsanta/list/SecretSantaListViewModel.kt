package com.example.secretsanta.ui.feature.secretsanta.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.domain.model.SecretSanta
import com.example.secretsanta.domain.repository.SecretSantaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SecretSantaListViewModel @Inject constructor(
    private val secretSantaRepository: SecretSantaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SecretSantaListState())
    val state = _state.asStateFlow()

    init {
        // TODO: Get current user ID from preferences
        loadSecretSantas("current_user_id")
    }

    private fun loadSecretSantas(userId: String) {
        secretSantaRepository.getSecretSantas(userId)
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