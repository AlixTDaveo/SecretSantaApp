package com.example.secretsanta.domain.repository

import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.domain.model.SecretSanta
import kotlinx.coroutines.flow.Flow

interface SecretSantaRepository {
    fun getSecretSantas(userId: String): Flow<List<SecretSanta>>
    fun getSecretSantaById(santaId: String): Flow<SecretSanta?>
    fun getAllSecretSantasForUser(userId: String, userEmail: String): Flow<List<SecretSanta>> // NOUVEAU
    suspend fun createSecretSanta(secretSanta: SecretSanta): Resource<SecretSanta>
    suspend fun updateSecretSanta(secretSanta: SecretSanta): Resource<SecretSanta>
    suspend fun deleteSecretSanta(santaId: String): Resource<Unit>
    suspend fun removeParticipant(santaId: String, participantId: String): Resource<Unit> // NOUVEAU
    suspend fun syncSecretSantas(userId: String): Resource<Unit>
}