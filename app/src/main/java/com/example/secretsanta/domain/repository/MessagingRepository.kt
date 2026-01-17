package com.example.secretsanta.domain.repository

import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.domain.model.Conversation
import com.example.secretsanta.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessagingRepository {
    fun observeConversations(currentUid: String): Flow<List<Conversation>>
    fun observeMessages(conversationId: String): Flow<List<Message>>

    suspend fun ensureConversationExists(
        conversationId: String,
        secretSantaId: String,
        uidA: String,
        uidB: String
    ): Resource<Unit>

    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String
    ): Resource<Unit>

    suspend fun findUserIdByEmail(email: String): String?

}
