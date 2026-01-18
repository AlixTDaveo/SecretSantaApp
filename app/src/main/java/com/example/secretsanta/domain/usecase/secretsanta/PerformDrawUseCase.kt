package com.example.secretsanta.domain.usecase.secretsanta

import com.example.secretsanta.core.util.Resource
import com.example.secretsanta.domain.model.SecretSanta
import com.example.secretsanta.domain.repository.SecretSantaRepository
import javax.inject.Inject

class PerformDrawUseCase @Inject constructor(
    private val repository: SecretSantaRepository
) {
    suspend operator fun invoke(secretSanta: SecretSanta): Resource<SecretSanta> {
        if (secretSanta.participants.size < 2) {
            return Resource.Error("Au moins 2 participants requis")
        }

        // Algorithme de tirage au sort
        val participantIds = secretSanta.participants.map { it.id }
        val shuffled = participantIds.shuffled()

        val assignments = mutableMapOf<String, String>()
        for (i in participantIds.indices) {
            val giver = participantIds[i]
            val receiver = shuffled[i]

            // Évite qu'une personne se tire elle-même
            if (giver == receiver) {
                return Resource.Error("Erreur de tirage, réessayez")
            }

            assignments[giver] = receiver
        }

        val updatedSecretSanta = secretSanta.copy(
            drawDone = true,
            assignments = assignments
        )

        return repository.updateSecretSanta(updatedSecretSanta)
    }
}