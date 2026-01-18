package com.example.secretsanta.domain.model

data class SecretSanta(
    val id: String,
    val name: String,
    val deadline: Long,
    val participants: List<Participant>,
    val creatorId: String,
    val drawDone: Boolean = false,
    val assignments: Map<String, String> = emptyMap(),
    val budget: String? = null,
    val description: String? = null
)