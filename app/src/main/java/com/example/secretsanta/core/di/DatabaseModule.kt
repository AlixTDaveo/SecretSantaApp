package com.example.secretsanta.core.di

import com.example.secretsanta.core.database.SecretSantaDatabase
import com.example.secretsanta.data.local.dao.SecretSantaDao
import com.example.secretsanta.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideUserDao(database: SecretSantaDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideSecretSantaDao(database: SecretSantaDatabase): SecretSantaDao {
        return database.secretSantaDao()
    }
}