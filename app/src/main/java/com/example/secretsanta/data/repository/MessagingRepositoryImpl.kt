package com.example.secretsanta.data.repository

import android.util.Log
import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.domain.model.Conversation
import com.example.secretsanta.domain.model.Message
import com.example.secretsanta.domain.repository.MessagingRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MessagingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MessagingRepository {

    override fun observeConversations(currentUid: String): Flow<List<Conversation>> = callbackFlow {
        val registration = firestore.collection("conversations")
            .whereArrayContains("participants", currentUid)
            .orderBy("lastMessageAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val conversations = snapshot?.documents?.map { doc ->
                    Conversation(
                        id = doc.id,
                        secretSantaId = doc.getString("secretSantaId") ?: "",
                        participants = doc.get("participants") as? List<String> ?: emptyList(),
                        createdAt = (doc.getLong("createdAt") ?: 0L),
                        lastMessage = doc.getString("lastMessage"),
                        lastMessageAt = doc.getLong("lastMessageAt"),
                        lastSenderId = doc.getString("lastSenderId")
                    )
                } ?: emptyList()

                trySend(conversations)
            }

        awaitClose { registration.remove() }
    }

    override fun observeMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val registration = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.map { doc ->
                    Message(
                        id = doc.id,
                        conversationId = conversationId,
                        senderId = doc.getString("senderId") ?: "",
                        text = doc.getString("text") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { registration.remove() }
    }

    override suspend fun ensureConversationExists(
        conversationId: String,
        secretSantaId: String,
        uidA: String,
        uidB: String
    ): Resource<Unit> {
        return try {
            val ref = firestore.collection("conversations").document(conversationId)

            // Pas de get() (sinon PERMISSION_DENIED sur doc non existant)
            ref.set(
                mapOf(
                    "secretSantaId" to secretSantaId,
                    "participants" to listOf(uidA, uidB),
                    "createdAt" to System.currentTimeMillis(),
                    "lastMessage" to null,
                    "lastMessageAt" to null,
                    "lastSenderId" to null
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create conversation")
        }
    }



    override suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String
    ): Resource<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val convoRef = firestore.collection("conversations").document(conversationId)
            val msgRef = convoRef.collection("messages").document()

            firestore.runBatch { batch ->
                batch.set(
                    msgRef,
                    mapOf(
                        "senderId" to senderId,
                        "text" to text,
                        "createdAt" to now
                    )
                )

                batch.update(
                    convoRef,
                    mapOf(
                        "lastMessage" to text,
                        "lastMessageAt" to now,
                        "lastSenderId" to senderId,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send message")
        }
    }
    override suspend fun findUserIdByEmail(email: String): String? {
        return try {
            val emailLower = email.trim().lowercase()

            val snapshot = firestore
                .collection("public_users") // 🔥 IMPORTANT
                .whereEqualTo("emailLower", emailLower)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.documents.first().id
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }


}
