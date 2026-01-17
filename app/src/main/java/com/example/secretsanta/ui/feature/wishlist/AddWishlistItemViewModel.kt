package com.example.secretsanta.ui.feature.wishlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.domain.model.ProductSuggestion
import com.example.secretsanta.domain.model.WishlistItem
import com.example.secretsanta.domain.repository.WishlistRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddWishlistItemViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    companion object {
        private const val TAG = "AddWishlistItemVM"
    }

    // IMPORTANT : Utiliser le vrai Firebase UID
    private val currentUserId: String = firebaseAuth.currentUser?.uid ?: ""

    private val _state = MutableStateFlow(AddWishlistItemState())
    val state = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        Log.d(TAG, "╔══════════════════════════════════════")
        Log.d(TAG, "║ INIT AddWishlistItemViewModel")
        Log.d(TAG, "║ Firebase currentUser: ${firebaseAuth.currentUser?.uid}")
        Log.d(TAG, "║ currentUserId: $currentUserId")
        Log.d(TAG, "╚══════════════════════════════════════")

        if (currentUserId.isEmpty()) {
            Log.e(TAG, "⚠️ ATTENTION: currentUserId est VIDE! L'utilisateur n'est pas connecté!")
        }
    }

    fun onEvent(event: AddWishlistItemEvent) {
        when (event) {
            is AddWishlistItemEvent.TitleChanged -> _state.value = _state.value.copy(title = event.title)
            is AddWishlistItemEvent.PriceChanged -> {
                val filtered = event.price.filter { it.isDigit() }
                _state.value = _state.value.copy(price = filtered)
            }
            is AddWishlistItemEvent.LinkChanged -> _state.value = _state.value.copy(link = event.link)
            is AddWishlistItemEvent.SearchQueryChanged -> {
                _state.value = _state.value.copy(searchQuery = event.query)
                searchProducts(event.query)
            }
            is AddWishlistItemEvent.AddManualItem -> addManualItem()
            is AddWishlistItemEvent.AddFromSuggestion -> addFromSuggestion(event.suggestion)
            is AddWishlistItemEvent.ClearSearch -> {
                searchJob?.cancel()
                _state.value = _state.value.copy(
                    searchQuery = "",
                    suggestions = emptyList(),
                    isSearching = false,
                    noResults = false
                )
            }
        }
    }

    private fun searchProducts(query: String) {
        searchJob?.cancel()
        if (query.length < 2) {
            _state.value = _state.value.copy(suggestions = emptyList(), isSearching = false, noResults = false)
            return
        }
        _state.value = _state.value.copy(isSearching = true, noResults = false)
        searchJob = viewModelScope.launch {
            delay(400)
            Log.d(TAG, "🔍 Searching: $query")
            when (val result = wishlistRepository.searchProducts(query)) {
                is Resource.Success -> {
                    val products = result.data ?: emptyList()
                    Log.d(TAG, "✅ Found ${products.size} products")
                    _state.value = _state.value.copy(
                        suggestions = products,
                        isSearching = false,
                        noResults = products.isEmpty()
                    )
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ Search error: ${result.message}")
                    _state.value = _state.value.copy(isSearching = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun addManualItem() {
        val title = _state.value.title.trim()
        if (title.isBlank()) {
            _state.value = _state.value.copy(error = "Le titre est requis")
            return
        }

        if (currentUserId.isEmpty()) {
            Log.e(TAG, "❌ Cannot add item: user not logged in!")
            _state.value = _state.value.copy(error = "Erreur: utilisateur non connecté")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val item = WishlistItem(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,  // Firebase UID
                title = title,
                priceEstimate = _state.value.price.toIntOrNull(),
                link = _state.value.link.ifBlank { null },
                order = 0
            )

            Log.d(TAG, "╔══════════════════════════════════════")
            Log.d(TAG, "║ ADDING MANUAL ITEM")
            Log.d(TAG, "║ title: ${item.title}")
            Log.d(TAG, "║ userId: ${item.userId}")
            Log.d(TAG, "║ id: ${item.id}")
            Log.d(TAG, "╚══════════════════════════════════════")

            when (val result = wishlistRepository.addItem(item)) {
                is Resource.Success -> {
                    Log.d(TAG, "✅ Item added successfully!")
                    _state.value = _state.value.copy(
                        title = "",
                        price = "",
                        link = "",
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ Add error: ${result.message}")
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun addFromSuggestion(suggestion: ProductSuggestion) {
        if (currentUserId.isEmpty()) {
            Log.e(TAG, "❌ Cannot add: user not logged in!")
            _state.value = _state.value.copy(error = "Erreur: utilisateur non connecté")
            return
        }

        viewModelScope.launch {
            val item = WishlistItem(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                title = suggestion.title,
                priceEstimate = suggestion.price.toInt(),
                link = null,
                imageUrl = suggestion.imageUrl
            )

            Log.d(TAG, "➕ Adding suggestion: ${suggestion.title} for user: $currentUserId")

            when (val result = wishlistRepository.addItem(item)) {
                is Resource.Success -> {
                    Log.d(TAG, "✅ Suggestion added!")
                    _state.value = _state.value.copy(
                        addedSuggestionIds = _state.value.addedSuggestionIds + suggestion.id
                    )
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ Error: ${result.message}")
                    _state.value = _state.value.copy(error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }
}

data class AddWishlistItemState(
    val title: String = "",
    val price: String = "",
    val link: String = "",
    val searchQuery: String = "",
    val suggestions: List<ProductSuggestion> = emptyList(),
    val addedSuggestionIds: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val noResults: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

sealed class AddWishlistItemEvent {
    data class TitleChanged(val title: String) : AddWishlistItemEvent()
    data class PriceChanged(val price: String) : AddWishlistItemEvent()
    data class LinkChanged(val link: String) : AddWishlistItemEvent()
    data class SearchQueryChanged(val query: String) : AddWishlistItemEvent()
    object AddManualItem : AddWishlistItemEvent()
    data class AddFromSuggestion(val suggestion: ProductSuggestion) : AddWishlistItemEvent()
    object ClearSearch : AddWishlistItemEvent()
}