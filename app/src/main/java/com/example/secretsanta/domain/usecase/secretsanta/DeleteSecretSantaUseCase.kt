package com.example.secretsanta.domain.usecase.secretsanta

import com.example.secretsanta.domain.repository.SecretSantaRepository
import javax.inject.Inject

class DeleteSecretSantaUseCase @Inject constructor(
    private val repository: SecretSantaRepository
) {
    suspend operator fun invoke(santaId: String) =
        repository.deleteSecretSanta(santaId)
}