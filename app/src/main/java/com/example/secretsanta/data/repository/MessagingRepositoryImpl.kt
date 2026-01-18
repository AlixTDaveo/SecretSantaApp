package com.example.secretsanta.data.repository

import android.util.Log
import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.domain.model.Conversation
import com.example.secretsanta.domain.model.Message
import com.example.secretsanta.domain.repository.MessagingRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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

                    // ✅ FIX : Gestion spécifique du logout (comme SecretSantaRepository)
                    if (error is FirebaseFirestoreException &&
                        error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w("MessagingRepo", "⚠️ User logged out, closing listener gracefully")
                        trySend(emptyList())
                        close()
                        return@addSnapshotListener
                    }

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
        Log.d("MessagingRepo", "🎧 Starting observeMessages for conversation: $conversationId")

        val registration = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MessagingRepo", "❌ Messages listener error", error)

                    // ✅ FIX : Même protection que conversations
                    if (error is FirebaseFirestoreException &&
                        error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w("MessagingRepo", "⚠️ User logged out during chat, closing gracefully")
                        trySend(emptyList())
                        close()
                        return@addSnapshotListener
                    }

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

                Log.d("MessagingRepo", "📥 Received ${messages.size} messages")
                trySend(messages)
            }

        awaitClose {
            Log.d("MessagingRepo", "🔌 Removing messages listener")
            registration.remove()
        }
    }

    override suspend fun ensureConversationExists(
        conversationId: String,
        secretSantaId: String,
        uidA: String,
        uidB: String
    ): Resource<Unit> {
        return try {
            Log.d("MessagingRepo", "🔧 Ensuring conversation exists: $conversationId")

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

            Log.d("MessagingRepo", "✅ Conversation ensured: $conversationId")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("MessagingRepo", "❌ Failed to ensure conversation", e)
            Resource.Error(e.message ?: "Failed to create conversation")
        }
    }

    override suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String
    ): Resource<Unit> {
        return try {
            Log.d("MessagingRepo", "📤 Sending message to $conversationId from $senderId")

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

            Log.d("MessagingRepo", "✅ Message sent successfully")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("MessagingRepo", "❌ Failed to send message", e)
            Resource.Error(e.message ?: "Failed to send message")
        }
    }

    override suspend fun findUserIdByEmail(email: String): String? {
        return try {
            Log.d("MessagingRepo", "🔍 Searching userId for email: $email")

            val emailQuery = email.trim().lowercase()

            // ✅ OPTIMISATION : Utilise public_users avec index emailLower au lieu de télécharger TOUS les users
            // Avant : firestore.collection("users").get() → télécharge TOUT (lent + coûteux)
            // Après : requête indexée sur 1 seul document (100x plus rapide)
            val snapshot = firestore
                .collection("public_users")
                .whereEqualTo("emailLower", emailQuery)
                .limit(1)
                .get()
                .await()

            val userId = snapshot.documents.firstOrNull()?.id

            if (userId != null) {
                Log.d("MessagingRepo", "✅ Found userId: $userId for email: $email")
            } else {
                Log.w("MessagingRepo", "⚠️ No user found for email: $email")
            }

            userId
        } catch (e: Exception) {
            Log.e("MessagingRepo", "❌ Error finding user by email", e)
            null
        }
    }

    // ✅ NOUVEAU : Supprime toutes les conversations (+ messages) d'un Secret Santa
    override suspend fun deleteConversationsBySecretSanta(secretSantaId: String): Resource<Unit> {
        return try {
            Log.d("MessagingRepo", "🗑️ Deleting conversations for Secret Santa: $secretSantaId")

            // 1️⃣ Récupère toutes les conversations liées à ce Secret Santa
            val conversationsSnapshot = firestore.collection("conversations")
                .whereEqualTo("secretSantaId", secretSantaId)
                .get()
                .await()

            if (conversationsSnapshot.isEmpty) {
                Log.d("MessagingRepo", "ℹ️ No conversations found for Secret Santa: $secretSantaId")
                return Resource.Success(Unit)
            }

            Log.d("MessagingRepo", "📋 Found ${conversationsSnapshot.size()} conversations to delete")

            // 2️⃣ Supprime chaque conversation et ses messages
            conversationsSnapshot.documents.forEach { conversationDoc ->
                val conversationId = conversationDoc.id
                Log.d("MessagingRepo", "  🗑️ Deleting conversation: $conversationId")

                // Supprime tous les messages de cette conversation
                val messagesSnapshot = conversationDoc.reference
                    .collection("messages")
                    .get()
                    .await()

                Log.d("MessagingRepo", "    📧 Deleting ${messagesSnapshot.size()} messages")

                messagesSnapshot.documents.forEach { messageDoc ->
                    messageDoc.reference.delete().await()
                }

                // Supprime la conversation elle-même
                conversationDoc.reference.delete().await()
                Log.d("MessagingRepo", "  ✅ Conversation deleted: $conversationId")
            }

            Log.d("MessagingRepo", "🟢 All conversations deleted successfully for Secret Santa: $secretSantaId")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("MessagingRepo", "❌ Error deleting conversations for Secret Santa: $secretSantaId", e)
            Resource.Error(e.message ?: "Failed to delete conversations")
        }
    }
}