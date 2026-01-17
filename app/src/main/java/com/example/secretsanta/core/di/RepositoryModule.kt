package com.example.secretsanta.core.di

import com.example.secretsanta.data.repository.AuthRepositoryImpl
import com.example.secretsanta.data.repository.SecretSantaRepositoryImpl
import com.example.secretsanta.domain.repository.AuthRepository
import com.example.secretsanta.domain.repository.SecretSantaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.secretsanta.data.remote.FakeStoreApi
import com.example.secretsanta.domain.repository.WishlistRepository
import com.example.secretsanta.data.repository.WishlistRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Provides

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSecretSantaRepository(
        secretSantaRepositoryImpl: SecretSantaRepositoryImpl
    ): SecretSantaRepository

    @Binds
    @Singleton
    abstract fun bindWishlistRepository(
        impl: WishlistRepositoryImpl
    ): WishlistRepository
}