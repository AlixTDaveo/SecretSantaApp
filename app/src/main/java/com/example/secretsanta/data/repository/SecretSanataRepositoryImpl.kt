package com.example.secretsanta.data.repository

import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.data.local.dao.SecretSantaDao
import com.example.secretsanta.data.local.entity.SecretSantaEntity
import com.example.secretsanta.domain.model.SecretSanta
import com.example.secretsanta.domain.repository.SecretSantaRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SecretSantaRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val secretSantaDao: SecretSantaDao
) : SecretSantaRepository {

    override fun getSecretSantas(userId: String): Flow<List<SecretSanta>> {
        return secretSantaDao.getSecretSantasByUser(userId).map { entities ->
            entities.map { entity ->
                SecretSanta(
                    id = entity.id,
                    name = entity.name,
                    deadline = entity.deadline,
                    participants = entity.participants.split(","),
                    creatorId = entity.creatorId
                )
            }
        }
    }

    override suspend fun createSecretSanta(secretSanta: SecretSanta): Resource<SecretSanta> {
        return try {
            // Save to Firestore
            firestore.collection("secret_santas")
                .document(secretSanta.id)
                .set(
                    mapOf(
                        "name" to secretSanta.name,
                        "deadline" to secretSanta.deadline,
                        "participants" to secretSanta.participants,
                        "creatorId" to secretSanta.creatorId
                    )
                ).await()

            // Save locally
            secretSantaDao.insertSecretSanta(
                SecretSantaEntity(
                    id = secretSanta.id,
                    name = secretSanta.name,
                    deadline = secretSanta.deadline,
                    participants = secretSanta.participants.joinToString(","),
                    creatorId = secretSanta.creatorId
                )
            )

            Resource.Success(secretSanta)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create Secret Santa")
        }
    }

    override suspend fun syncSecretSantas(userId: String): Resource<Unit> {
        return try {
            val snapshot = firestore.collection("secret_santas")
                .whereEqualTo("creatorId", userId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                val entity = SecretSantaEntity(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    deadline = doc.getLong("deadline") ?: 0L,
                    participants = (doc.get("participants") as? List<*>)
                        ?.joinToString(",") ?: "",
                    creatorId = doc.getString("creatorId") ?: ""
                )
                secretSantaDao.insertSecretSanta(entity)
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sync failed")
        }
    }
}