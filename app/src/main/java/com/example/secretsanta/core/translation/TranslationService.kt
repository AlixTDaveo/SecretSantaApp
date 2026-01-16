package com.example.secretsanta.core.translation

import android.content.Context
import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🌐 Service de traduction automatique avec Google ML Kit
 *
 * Traduit automatiquement les textes en fonction de la langue du système
 * Langues supportées : Français (base), Anglais, Espagnol
 */
@Singleton
class TranslationService @Inject constructor(
    private val context: Context
) {
    private val translators = mutableMapOf<String, com.google.mlkit.nl.translate.Translator>()

    /**
     * Détecte la langue du système
     */
    fun getCurrentLanguage(): String {
        val locale = Locale.getDefault().language
        return when (locale) {
            "en" -> TranslateLanguage.ENGLISH
            "es" -> TranslateLanguage.SPANISH
            else -> TranslateLanguage.FRENCH // Par défaut français
        }
    }

    /**
     * Traduit un texte depuis le français vers la langue actuelle
     */
    suspend fun translate(text: String): String {
        val targetLanguage = getCurrentLanguage()

        // Si c'est déjà en français, pas besoin de traduire
        if (targetLanguage == TranslateLanguage.FRENCH) {
            return text
        }

        return try {
            val translator = getOrCreateTranslator(targetLanguage)
            translator.translate(text).await()
        } catch (e: Exception) {
            Log.e("TranslationService", "Erreur de traduction", e)
            text // Retourne le texte original en cas d'erreur
        }
    }

    /**
     * Obtient ou crée un traducteur pour une langue donnée
     */
    private suspend fun getOrCreateTranslator(targetLanguage: String): com.google.mlkit.nl.translate.Translator {
        val key = "fr-$targetLanguage"

        return translators.getOrPut(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.FRENCH)
                .setTargetLanguage(targetLanguage)
                .build()

            val translator = Translation.getClient(options)

            // Télécharge le modèle si nécessaire
            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()

            translator.downloadModelIfNeeded(conditions).await()

            translator
        }
    }

    /**
     * Libère les ressources des traducteurs
     */
    fun cleanup() {
        translators.values.forEach { it.close() }
        translators.clear()
    }

    /**
     * Extension function pour traduire facilement
     */
    suspend fun String.tr(): String = translate(this)
}