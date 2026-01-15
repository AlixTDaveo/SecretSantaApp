package com.example.secretsanta.domain.repository

import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(email: String, password: String, displayName: String): Resource<User>
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun logout()
    fun getCurrentUser(): Flow<User?>
    suspend fun updateDisplayName(displayName: String): Resource<Unit>
    suspend fun changePassword(oldPassword: String, newPassword: String): Resource<Unit>
}