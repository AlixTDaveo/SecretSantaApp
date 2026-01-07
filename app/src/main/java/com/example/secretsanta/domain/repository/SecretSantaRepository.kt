package com.example.secretsanta.domain.repository

import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.domain.model.SecretSanta
import kotlinx.coroutines.flow.Flow

interface SecretSantaRepository {
    fun getSecretSantas(userId: String): Flow<List<SecretSanta>>
    suspend fun createSecretSanta(secretSanta: SecretSanta): Resource<SecretSanta>
    suspend fun syncSecretSantas(userId: String): Resource<Unit>
}