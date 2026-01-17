package com.example.secretsanta.domain.model

data class Conversation(
    val id: String,
    val secretSantaId: String,
    val participants: List<String>, // uid firebase
    val createdAt: Long = 0L,
    val lastMessage: String? = null,
    val lastMessageAt: Long? = null,
    val lastSenderId: String? = null
)
