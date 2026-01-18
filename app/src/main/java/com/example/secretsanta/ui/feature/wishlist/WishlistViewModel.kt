package com.example.secretsanta.ui.feature.wishlist

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.domain.model.WishlistItem
import com.example.secretsanta.domain.repository.WishlistRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository,
    private val firebaseAuth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "WishlistViewModel"
    }

    // Paramètres de navigation
    private val targetUserId: String? = savedStateHandle.get<String>("userId")
    private val secretSantaId: String? = savedStateHandle.get<String>("santaId")

    // IMPORTANT : Utiliser le vrai Firebase UID
    private val currentUserId: String = firebaseAuth.currentUser?.uid ?: ""

    // ID de la wishlist à charger
    private val wishlistOwnerId: String = targetUserId ?: currentUserId

    private val _state = MutableStateFlow(WishlistState())
    val state = _state.asStateFlow()

    init {
        Log.d(TAG, "╔══════════════════════════════════════")
        Log.d(TAG, "║ INIT WishlistViewModel")
        Log.d(TAG, "║ Firebase currentUser: ${firebaseAuth.currentUser?.uid}")
        Log.d(TAG, "║ currentUserId: $currentUserId")
        Log.d(TAG, "║ targetUserId (filleul): $targetUserId")
        Log.d(TAG, "║ wishlistOwnerId: $wishlistOwnerId")
        Log.d(TAG, "║ secretSantaId: $secretSantaId")
        Log.d(TAG, "║ isViewingOtherUser: ${targetUserId != null}")
        Log.d(TAG, "╚══════════════════════════════════════")

        if (currentUserId.isEmpty()) {
            Log.e(TAG, "⚠️ ATTENTION: currentUserId est VIDE! L'utilisateur n'est pas connecté!")
        }

        _state.value = _state.value.copy(
            currentUserId = currentUserId,
            secretSantaId = secretSantaId,
            isViewingOtherUser = targetUserId != null
        )

        loadWishlist()
    }

    private fun loadWishlist() {
        Log.d(TAG, "🔄 Loading wishlist for: $wishlistOwnerId")

        viewModelScope.launch {
            wishlistRepository.getUserWishlist(wishlistOwnerId).collect { items ->
                Log.d(TAG, "📥 Received ${items.size} items")
                items.forEach { item ->
                    Log.d(TAG, "  - ${item.title} (reservedBy: ${item.reservedBy})")
                }
                _state.value = _state.value.copy(items = items, isLoading = false)
            }
        }
    }

    fun onEvent(event: WishlistEvent) {
        when (event) {
            is WishlistEvent.DeleteItem -> deleteItem(event.itemId)
            is WishlistEvent.ToggleReservation -> toggleReservation(event.itemId)
            is WishlistEvent.DismissError -> _state.value = _state.value.copy(error = null)
        }
    }

    private fun deleteItem(itemId: String) {
        if (_state.value.isViewingOtherUser) {
            Log.w(TAG, "Cannot delete from other user's wishlist")
            return
        }

        Log.d(TAG, "🗑️ Deleting item: $itemId")
        viewModelScope.launch {
            when (val result = wishlistRepository.deleteItem(itemId, currentUserId)) {
                is Resource.Success -> Log.d(TAG, "✅ Deleted")
                is Resource.Error -> {
                    Log.e(TAG, "❌ Delete error: ${result.message}")
                    _state.value = _state.value.copy(error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun toggleReservation(itemId: String) {
        if (!_state.value.isViewingOtherUser) {
            Log.w(TAG, "Cannot reserve on own wishlist")
            return
        }

        val santaId = secretSantaId
        if (santaId == null) {
            Log.e(TAG, "❌ No secretSantaId!")
            return
        }

        val ownerId = targetUserId
        if (ownerId == null) {
            Log.e(TAG, "❌ No targetUserId!")
            return
        }

        Log.d(TAG, "🔄 Toggle reservation - item: $itemId, owner: $ownerId, reserver: $currentUserId")
        viewModelScope.launch {
            when (val result = wishlistRepository.toggleReservation(itemId, ownerId, currentUserId, santaId)) {
                is Resource.Success -> Log.d(TAG, "✅ Reservation toggled")
                is Resource.Error -> {
                    Log.e(TAG, "❌ Reservation error: ${result.message}")
                    _state.value = _state.value.copy(error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }
}

data class WishlistState(
    val items: List<WishlistItem> = emptyList(),
    val isLoading: Boolean = true,
    val isViewingOtherUser: Boolean = false,
    val currentUserId: String = "",
    val secretSantaId: String? = null,
    val error: String? = null
)

sealed class WishlistEvent {
    data class DeleteItem(val itemId: String) : WishlistEvent()
    data class ToggleReservation(val itemId: String) : WishlistEvent()
    object DismissError : WishlistEvent()
}