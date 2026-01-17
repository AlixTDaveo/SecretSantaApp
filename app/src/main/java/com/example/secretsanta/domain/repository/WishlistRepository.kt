package com.example.secretsanta.domain.repository

import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.domain.model.ProductSuggestion
import com.example.secretsanta.domain.model.WishlistItem
import kotlinx.coroutines.flow.Flow

interface WishlistRepository {

    // ========== WISHLIST CRUD ==========

    fun getMyWishlist(userId: String): Flow<List<WishlistItem>>

    fun getUserWishlist(userId: String): Flow<List<WishlistItem>>

    suspend fun addItem(item: WishlistItem): Resource<Unit>

    suspend fun deleteItem(itemId: String, userId: String): Resource<Unit>

    // ========== RESERVATION ==========

    suspend fun toggleReservation(
        itemId: String,
        ownerId: String,
        reserverId: String,
        secretSantaId: String
    ): Resource<Unit>

    // ========== RECHERCHE PRODUITS ==========

    suspend fun searchProducts(query: String): Resource<List<ProductSuggestion>>
}