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
        Log.d("MessagingRepo", "🎧 Starting observeConversations for uid: $currentUid")

        val registration = firestore.collection("conversations")
            .whereArrayContains("participants", currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MessagingRepo", "❌ Listener error", error)
                    close(error)
                    return@addSnapshotListener
                }

                Log.d("MessagingRepo", "📥 Received ${snapshot?.documents?.size ?: 0} conversations")

                val conversations = snapshot?.documents?.map { doc ->
                    Log.d("MessagingRepo", "  - ${doc.id}: participants=${doc.get("participants")}")
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

                Log.d("MessagingRepo", "✅ Sending ${conversations.size} conversations to UI")
                trySend(conversations)
            }

        awaitClose {
            Log.d("MessagingRepo", "🔌 Removing listener")
            registration.remove()
        }
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
            Log.d("MessagingRepo", "🔍 Searching userId for email: $email")

            val emailQuery = email.trim().lowercase()

            // ✅ Cherche dans users avec le champ "email"
            val snapshot = firestore
                .collection("users")
                .get()  // Récupère tous les users
                .await()

            // Filtre manuellement (car whereEqualTo est case-sensitive)
            val matchingDoc = snapshot.documents.firstOrNull { doc ->
                val userEmail = doc.getString("email")?.trim()?.lowercase()
                userEmail == emailQuery
            }

            val userId = matchingDoc?.id
            Log.d("MessagingRepo", if (userId != null) {
                "✅ Found userId: $userId"
            } else {
                "❌ No user found for $email"
            })

            userId
        } catch (e: Exception) {
            Log.e("MessagingRepo", "❌ Error finding user by email", e)
            null
        }
    }


}
