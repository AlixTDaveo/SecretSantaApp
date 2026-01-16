package com.example.secretsanta.di

import android.content.Context
import com.example.secretsanta.core.translation.TranslationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TranslationModule {

    @Provides
    @Singleton
    fun provideTranslationService(
        @ApplicationContext context: Context
    ): TranslationService {
        return TranslationService(context)
    }
}