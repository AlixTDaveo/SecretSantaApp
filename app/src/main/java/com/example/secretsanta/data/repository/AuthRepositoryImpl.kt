package com.example.secretsanta.data.repository

import com.example.secretsanta.core.datastore.PreferencesManager
import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.data.local.dao.UserDao
import com.example.secretsanta.data.local.entity.UserEntity
import com.example.secretsanta.domain.model.User
import com.example.secretsanta.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import android.util.Log
import com.google.firebase.auth.EmailAuthProvider

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao,
    private val preferencesManager: PreferencesManager
) : AuthRepository {

    override suspend fun register(email: String, password: String, displayName: String): Resource<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("Registration failed")

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            val user = User(
                id = firebaseUser.uid,
                email = email,
                displayName = displayName
            )

            // Save to Firestore
            firestore.collection("users").document(firebaseUser.uid).set(
                mapOf(
                    "email" to email,
                    "displayName" to displayName
                )
            ).await()
            firestore.collection("public_users").document(firebaseUser.uid).set(
                mapOf(
                    "emailLower" to email.trim().lowercase(),
                    "displayName" to displayName
                )
            ).await()

            // NOUVEAU : Vérifier les invitations en attente
            checkPendingInvitations(firebaseUser.uid, email)

            // Save locally
            userDao.insertUser(
                UserEntity(
                    id = firebaseUser.uid,
                    email = email,
                    displayName = displayName
                )
            )

            preferencesManager.saveUserId(firebaseUser.uid)

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    private suspend fun checkPendingInvitations(userId: String, email: String) {
        try {
            // Récupère toutes les invitations pour cet email
            val invitations = firestore.collection("invitations")
                .whereEqualTo("email", email.lowercase())
                .whereEqualTo("status", "pending")
                .get()
                .await()

            invitations.documents.forEach { doc ->
                val secretSantaId = doc.getString("secretSantaId") ?: return@forEach

                // Marque l'invitation comme acceptée
                doc.reference.update("status", "accepted", "acceptedBy", userId).await()

                // Ajoute l'utilisateur aux participants du Secret Santa
                val secretSantaDoc = firestore.collection("secret_santas")
                    .document(secretSantaId)
                    .get()
                    .await()

                if (secretSantaDoc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val participants = secretSantaDoc.get("participants") as? MutableList<Map<String, Any>> ?: mutableListOf()

                    // Met à jour le participant avec son userId
                    val updatedParticipants = participants.map { p ->
                        if ((p["email"] as? String)?.equals(email, ignoreCase = true) == true) {
                            p.toMutableMap().apply {
                                put("userId", userId)
                            }
                        } else {
                            p
                        }
                    }

                    firestore.collection("secret_santas")
                        .document(secretSantaId)
                        .update("participants", updatedParticipants)
                        .await()
                }
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error checking invitations", e)
        }
    }

    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            Log.d("AuthRepository", "Login attempt for: $email")

            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("Login failed")

            Log.d("AuthRepository", "Firebase login successful: ${firebaseUser.uid}")

            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: email,
                displayName = firebaseUser.displayName ?: "User"
            )
            firestore.collection("public_users").document(firebaseUser.uid).set(
                mapOf(
                    "emailLower" to user.email.trim().lowercase(),
                    "displayName" to user.displayName
                )
            ).await()
            // Save locally
            userDao.insertUser(
                UserEntity(
                    id = firebaseUser.uid,
                    email = user.email,
                    displayName = user.displayName
                )
            )

            preferencesManager.saveUserId(firebaseUser.uid)

            Log.d("AuthRepository", "User saved locally")

            Resource.Success(user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login error", e)
            Resource.Error(e.message ?: "Login failed")
        }
    }

    override suspend fun logout() {
        Log.d("AuthRepository", "🔴 LOGOUT - Début")
        firebaseAuth.signOut()
        Log.d("AuthRepository", "✅ Firebase signOut OK")
        userDao.clearAll()
        Log.d("AuthRepository", "✅ Room clearAll OK")
        preferencesManager.clearUserData()
        Log.d("AuthRepository", "✅ DataStore cleared OK")
        Log.d("AuthRepository", "🟢 LOGOUT - Terminé avec succès")
    }

    override fun getCurrentUser(): Flow<User?> {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(null)
        return userDao.getUserById(currentUserId).map { entity ->
            entity?.let {
                User(
                    id = it.id,
                    email = it.email,
                    displayName = it.displayName
                )
            }
        }
    }
    override suspend fun updateDisplayName(displayName: String): Resource<Unit> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return Resource.Error("Not authenticated")

            // Update FirebaseAuth profile
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Update Firestore (merge)
            firestore.collection("users").document(firebaseUser.uid)
                .set(mapOf("displayName" to displayName), com.google.firebase.firestore.SetOptions.merge())
                .await()

            // Update Room (source de vérité UI)
            val email = firebaseUser.email ?: ""
            userDao.insertUser(
                UserEntity(
                    id = firebaseUser.uid,
                    email = email,
                    displayName = displayName
                )
            )
            firestore.collection("public_users").document(firebaseUser.uid)
                .set(mapOf("displayName" to displayName), com.google.firebase.firestore.SetOptions.merge())
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Update display name failed")
        }
    }
    override suspend fun changePassword(oldPassword: String, newPassword: String): Resource<Unit> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return Resource.Error("Not authenticated")
            val email = firebaseUser.email ?: return Resource.Error("Email not available")

            // Re-auth
            val credential = EmailAuthProvider.getCredential(email, oldPassword)
            firebaseUser.reauthenticate(credential).await()

            // Update password
            firebaseUser.updatePassword(newPassword).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Password update failed")
        }
    }
}