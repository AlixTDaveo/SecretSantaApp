package com.example.secretsanta.domain.usecase.auth

import com.example.secretsanta.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, displayName: String) =
        authRepository.register(email, password, displayName)
}