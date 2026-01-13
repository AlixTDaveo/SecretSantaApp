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
                    _state.value = _state.value.copy(
                        secretSanta = secretSanta,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onEvent(event: SecretSantaDetailsEvent) {
        when (event) {
            is SecretSantaDetailsEvent.PerformDraw -> {
                performDraw()
            }
            is SecretSantaDetailsEvent.DeleteSecretSanta -> {
                deleteSecretSanta()
            }
            is SecretSantaDetailsEvent.DismissError -> {
                _state.value = _state.value.copy(error = null)
            }
        }
    }

    private fun performDraw() {
        viewModelScope.launch {
            try {
                val secretSanta = _state.value.secretSanta ?: return@launch

                _state.value = _state.value.copy(isPerformingDraw = true, error = null)

                Log.d("DetailsVM", "Performing draw for: ${secretSanta.name}")

                val result = performDrawUseCase(secretSanta)

                when (result) {
                    is Resource.Success -> {
                        Log.d("DetailsVM", "Draw successful")
                        _state.value = _state.value.copy(
                            isPerformingDraw = false,
                            secretSanta = result.data
                        )
                    }
                    is Resource.Error -> {
                        Log.e("DetailsVM", "Draw error: ${result.message}")
                        _state.value = _state.value.copy(
                            isPerformingDraw = false,
                            error = result.message
                        )
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                Log.e("DetailsVM", "Exception", e)
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
}

data class SecretSantaDetailsState(
    val secretSanta: SecretSanta? = null,
    val isLoading: Boolean = true,
    val isPerformingDraw: Boolean = false,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
)

sealed class SecretSantaDetailsEvent {
    object PerformDraw : SecretSantaDetailsEvent()
    object DeleteSecretSanta : SecretSantaDetailsEvent()
    object DismissError : SecretSantaDetailsEvent()
}