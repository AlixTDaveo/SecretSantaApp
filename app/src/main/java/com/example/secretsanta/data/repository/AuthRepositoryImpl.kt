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

    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("Login failed")

            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: email,
                displayName = firebaseUser.displayName ?: "User"
            )

            // Save locally
            userDao.insertUser(
                UserEntity(
                    id = firebaseUser.uid,
                    email = user.email,
                    displayName = user.displayName
                )
            )

            preferencesManager.saveUserId(firebaseUser.uid)

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
        userDao.clearAll()
        preferencesManager.clearUserData()
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
}