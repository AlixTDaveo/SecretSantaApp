package com.example.secretsanta.domain.model

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val text: String,
    val createdAt: Long
)
