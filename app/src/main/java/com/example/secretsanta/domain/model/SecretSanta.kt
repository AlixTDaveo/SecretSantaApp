package com.example.secretsanta.domain.model

data class SecretSanta(
    val id: String,
    val name: String,
    val deadline: Long,
    val participants: List<String>,
    val creatorId: String
)