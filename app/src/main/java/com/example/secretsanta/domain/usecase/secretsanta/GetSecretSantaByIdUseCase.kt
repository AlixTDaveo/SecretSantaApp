package com.example.secretsanta.domain.usecase.secretsanta

import com.example.secretsanta.domain.model.SecretSanta
import com.example.secretsanta.domain.repository.SecretSantaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSecretSantaByIdUseCase @Inject constructor(
    private val repository: SecretSantaRepository
) {
    operator fun invoke(santaId: String): Flow<SecretSanta?> =
        repository.getSecretSantaById(santaId)
}