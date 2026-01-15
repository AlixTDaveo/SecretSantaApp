package com.example.secretsanta.data.repository

import android.util.Log
import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.data.local.dao.SecretSantaDao
import com.example.secretsanta.data.local.entity.SecretSantaEntity
import com.example.secretsanta.domain.model.Participant
import com.example.secretsanta.domain.model.SecretSanta
import com.example.secretsanta.domain.repository.SecretSantaRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SecretSantaRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val secretSantaDao: SecretSantaDao,
    private val gson: Gson
) : SecretSantaRepository {

    override fun getSecretSantas(userId: String): Flow<List<SecretSanta>> {
        return secretSantaDao.getSecretSantasByUser(userId).map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    override fun getSecretSantaById(santaId: String): Flow<SecretSanta?> {
        return secretSantaDao.getSecretSantaById(santaId).map { entity ->
            entity?.toDomain()
        }
    }

    override fun getAllSecretSantasForUser(userId: String, userEmail: String): Flow<List<SecretSanta>> {
        return combine(
            secretSantaDao.getSecretSantasByUser(userId),
            secretSantaDao.getAllSecretSantas()
        ) { createdByUser, allSantas ->
            val createdIds = createdByUser.map { it.id }.toSet()

            // Secret Santas créés par l'utilisateur
            val created = createdByUser.map { it.toDomain() }

            // Secret Santas où l'utilisateur est participant
            val participating = allSantas
                .filter { it.id !in createdIds }
                .map { it.toDomain() }
                .filter { santa ->
                    santa.participants.any {
                        it.email.equals(userEmail, ignoreCase = true)
                    }
                }

            (created + participating).distinctBy { it.id }
        }
    }

    override suspend fun createSecretSanta(secretSanta: SecretSanta): Resource<SecretSanta> {
        return try {
            val firestoreData = mapOf(
                "name" to secretSanta.name,
                "deadline" to secretSanta.deadline,
                "participants" to secretSanta.participants.map {
                    mapOf(
                        "id" to it.id,
                        "name" to it.name,
                        "email" to it.email,
                        "userId" to it.userId,
                        "isOrganizer" to it.isOrganizer
                    )
                },
                "creatorId" to secretSanta.creatorId,
                "drawDone" to secretSanta.drawDone,
                "assignments" to secretSanta.assignments,
                "budget" to secretSanta.budget,
                "description" to secretSanta.description
            )

            firestore.collection("secret_santas")
                .document(secretSanta.id)
                .set(firestoreData)
                .await()

            secretSantaDao.insertSecretSanta(secretSanta.toEntity())

            Resource.Success(secretSanta)
        } catch (e: Exception) {
            Log.e("SecretSantaRepo", "Error creating", e)
            Resource.Error(e.message ?: "Erreur de création")
        }
    }

    override suspend fun updateSecretSanta(secretSanta: SecretSanta): Resource<SecretSanta> {
        return try {
            val firestoreData = mapOf(
                "name" to secretSanta.name,
                "deadline" to secretSanta.deadline,
                "participants" to secretSanta.participants.map {
                    mapOf(
                        "id" to it.id,
                        "name" to it.name,
                        "email" to it.email,
                        "userId" to it.userId,
                        "isOrganizer" to it.isOrganizer
                    )
                },
                "creatorId" to secretSanta.creatorId,
                "drawDone" to secretSanta.drawDone,
                "assignments" to secretSanta.assignments,
                "budget" to secretSanta.budget,
                "description" to secretSanta.description
            )

            firestore.collection("secret_santas")
                .document(secretSanta.id)
                .update(firestoreData)
                .await()

            secretSantaDao.updateSecretSanta(secretSanta.toEntity())

            Resource.Success(secretSanta)
        } catch (e: Exception) {
            Log.e("SecretSantaRepo", "Error updating", e)
            Resource.Error(e.message ?: "Erreur de mise à jour")
        }
    }

    override suspend fun deleteSecretSanta(santaId: String): Resource<Unit> {
        return try {
            firestore.collection("secret_santas")
                .document(santaId)
                .delete()
                .await()

            secretSantaDao.deleteSecretSanta(santaId)

            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("SecretSantaRepo", "Error deleting", e)
            Resource.Error(e.message ?: "Erreur de suppression")
        }
    }

    override suspend fun removeParticipant(santaId: String, participantId: String): Resource<Unit> {
        return try {
            // Récupère le Secret Santa depuis Firestore
            val doc = firestore.collection("secret_santas")
                .document(santaId)
                .get()
                .await()

            if (!doc.exists()) {
                return Resource.Error("Secret Santa introuvable")
            }

            @Suppress("UNCHECKED_CAST")
            val participantsList = (doc.get("participants") as? List<Map<String, Any>>)?.map {
                Participant(
                    id = it["id"] as? String ?: "",
                    name = it["name"] as? String ?: "",
                    email = it["email"] as? String ?: "",
                    userId = it["userId"] as? String,
                    isOrganizer = it["isOrganizer"] as? Boolean ?: false
                )
            } ?: emptyList()

            // Retire le participant
            val updatedParticipants = participantsList.filter { it.id != participantId }

            // Met à jour Firestore
            firestore.collection("secret_santas")
                .document(santaId)
                .update("participants", updatedParticipants.map {
                    mapOf(
                        "id" to it.id,
                        "name" to it.name,
                        "email" to it.email,
                        "userId" to it.userId,
                        "isOrganizer" to it.isOrganizer
                    )
                })
                .await()

            // Met à jour Room
            val entity = secretSantaDao.getSecretSantaById(santaId)
            var localEntity: SecretSantaEntity? = null
            entity.collect { localEntity = it }

            if (localEntity != null) {
                val updatedEntity = localEntity!!.copy(
                    participants = gson.toJson(updatedParticipants)
                )
                secretSantaDao.updateSecretSanta(updatedEntity)
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("SecretSantaRepo", "Error removing participant", e)
            Resource.Error(e.message ?: "Erreur de suppression")
        }
    }

    override suspend fun syncSecretSantas(userId: String): Resource<Unit> {
        return try {
            val snapshot = firestore.collection("secret_santas")
                .whereEqualTo("creatorId", userId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                val data = doc.data ?: return@forEach

                @Suppress("UNCHECKED_CAST")
                val participantsList = (data["participants"] as? List<Map<String, Any>>)?.map {
                    Participant(
                        id = it["id"] as? String ?: "",
                        name = it["name"] as? String ?: "",
                        email = it["email"] as? String ?: "",
                        userId = it["userId"] as? String,
                        isOrganizer = it["isOrganizer"] as? Boolean ?: false
                    )
                } ?: emptyList()

                @Suppress("UNCHECKED_CAST")
                val assignmentsMap = data["assignments"] as? Map<String, String> ?: emptyMap()

                val secretSanta = SecretSanta(
                    id = doc.id,
                    name = data["name"] as? String ?: "",
                    deadline = data["deadline"] as? Long ?: 0L,
                    participants = participantsList,
                    creatorId = data["creatorId"] as? String ?: "",
                    drawDone = data["drawDone"] as? Boolean ?: false,
                    assignments = assignmentsMap,
                    budget = data["budget"] as? String,
                    description = data["description"] as? String
                )

                secretSantaDao.insertSecretSanta(secretSanta.toEntity())
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("SecretSantaRepo", "Error syncing", e)
            Resource.Error(e.message ?: "Erreur de synchronisation")
        }
    }

    private fun SecretSantaEntity.toDomain(): SecretSanta {
        val participantsType = object : TypeToken<List<Participant>>() {}.type
        val participantsList: List<Participant> = try {
            gson.fromJson(participants, participantsType)
        } catch (e: Exception) {
            emptyList()
        }

        val assignmentsType = object : TypeToken<Map<String, String>>() {}.type
        val assignmentsMap: Map<String, String> = try {
            gson.fromJson(assignments, assignmentsType)
        } catch (e: Exception) {
            emptyMap()
        }

        return SecretSanta(
            id = id,
            name = name,
            deadline = deadline,
            participants = participantsList,
            creatorId = creatorId,
            drawDone = drawDone,
            assignments = assignmentsMap,
            budget = budget,
            description = description
        )
    }

    private fun SecretSanta.toEntity(): SecretSantaEntity {
        return SecretSantaEntity(
            id = id,
            name = name,
            deadline = deadline,
            participants = gson.toJson(participants),
            creatorId = creatorId,
            drawDone = drawDone,
            assignments = gson.toJson(assignments),
            budget = budget,
            description = description
        )
    }
}