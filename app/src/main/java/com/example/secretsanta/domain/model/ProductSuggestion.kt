package com.example.secretsanta.domain.model

data class ProductSuggestion(
    val id: Int,
    val title: String,
    val price: Double,
    val imageUrl: String,
    val category: String
)