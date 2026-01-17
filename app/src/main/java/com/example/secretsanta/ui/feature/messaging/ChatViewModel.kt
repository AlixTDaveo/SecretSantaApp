package com.example.secretsanta.ui.feature.messaging

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.domain.model.Message
import com.example.secretsanta.domain.repository.MessagingRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatState(
    val isLoading: Boolean = true,
    val messages: List<Message> = emptyList(),
    val input: String = "",
    val isSending: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messagingRepository: MessagingRepository,
    private val firebaseAuth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val conversationId: String = savedStateHandle["conversationId"] ?: ""

    private val _state = MutableStateFlow(ChatState())
    val state = _state.asStateFlow()

    init {
        if (conversationId.isBlank()) {
            _state.value = ChatState(isLoading = false, error = "Missing conversationId")
        } else {
            messagingRepository.observeMessages(conversationId)
                .onEach { msgs ->
                    _state.value = _state.value.copy(isLoading = false, messages = msgs)
                }
                .catch { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
                .launchIn(viewModelScope)
        }
    }


    fun onInputChanged(v: String) {
        _state.value = _state.value.copy(input = v)
    }

    fun send() {
        val uid = firebaseAuth.currentUser?.uid.orEmpty()
        val text = _state.value.input.trim()
        if (uid.isBlank() || text.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSending = true, error = null)

            when (val r = messagingRepository.sendMessage(conversationId, uid, text)) {
                is Resource.Success -> _state.value = _state.value.copy(isSending = false, input = "")
                is Resource.Error -> _state.value = _state.value.copy(isSending = false, error = r.message)
                is Resource.Loading -> {}
            }
        }
    }
}
