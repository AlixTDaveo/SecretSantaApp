package com.example.secretsanta.domain.model

data class WishlistItem(
    val id: String,
    val userId: String,
    val title: String,
    val priceEstimate: Int?,
    val link: String?,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val order: Int = 0,
    val reservedBy: String? = null,
    val reservedInSecretSanta: String? = null
)