package com.example.secretsanta.domain.usecase.secretsanta

import com.example.secretsanta.domain.model.SecretSanta
import com.example.secretsanta.domain.repository.SecretSantaRepository
import javax.inject.Inject

class CreateSecretSantaUseCase @Inject constructor(
    private val repository: SecretSantaRepository
) {
    suspend operator fun invoke(secretSanta: SecretSanta) =
        repository.createSecretSanta(secretSanta)
}