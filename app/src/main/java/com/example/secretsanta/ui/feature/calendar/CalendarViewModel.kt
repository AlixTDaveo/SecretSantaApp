package com.example.secretsanta.ui.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.domain.model.SecretSanta
import com.example.secretsanta.domain.repository.SecretSantaRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import javax.inject.Inject

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val secretSantas: List<SecretSanta> = emptyList(),
    val selectedDate: LocalDate? = null,
    val selectedSantas: List<SecretSanta> = emptyList()
)

sealed interface CalendarEvent {
    data object PreviousMonth : CalendarEvent
    data object NextMonth : CalendarEvent
    data class DayClicked(val date: LocalDate) : CalendarEvent
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val secretSantaRepository: SecretSantaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CalendarUiState())
    val state: StateFlow<CalendarUiState> = _state.asStateFlow()

    init {
        observeSecretSantas()
    }

    private fun observeSecretSantas() {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            val email = FirebaseAuth.getInstance().currentUser?.email.orEmpty()

            if (uid.isBlank()) {
                _state.update { it.copy(secretSantas = emptyList()) }
                return@launch
            }

            secretSantaRepository.getSecretSantas(uid).collect { santas ->
                // ✅ FILTRE AUSSI SUR L'EMAIL
                val filteredSantas = santas.filter { santa ->
                    santa.participants.any { participant ->
                        participant.userId == uid ||
                                participant.email.equals(email, ignoreCase = true)
                    }
                }

                _state.update { it.copy(secretSantas = filteredSantas) }
            }
        }
    }

    fun onEvent(event: CalendarEvent) {
        when (event) {
            CalendarEvent.PreviousMonth -> {
                _state.update { it.copy(currentMonth = it.currentMonth.minusMonths(1)) }
            }

            CalendarEvent.NextMonth -> {
                _state.update { it.copy(currentMonth = it.currentMonth.plusMonths(1)) }
            }

            is CalendarEvent.DayClicked -> {
                val santasThatDay = _state.value.secretSantas.filter { santa ->
                    santa.deadline.toLocalDate() == event.date
                }

                _state.update {
                    it.copy(
                        selectedDate = event.date,
                        selectedSantas = santasThatDay
                    )
                }
            }
        }
    }
}

private fun Long.toLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()