package com.example.secretsanta.core.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.secretsanta.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null

    fun playSuccessSound() {
        try {
            // Libère le MediaPlayer précédent si existant
            mediaPlayer?.release()

            // Crée un nouveau MediaPlayer
            // IMPORTANT : Remplacez R.raw.jingle_bell par le nom de votre fichier
            mediaPlayer = MediaPlayer.create(context, R.raw.jingle_bells)

            mediaPlayer?.apply {
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
                start()
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error playing sound", e)
        }
    }

    fun playErrorSound() {
        // Optionnel : Son d'erreur différent
        try {
            mediaPlayer?.release()
            // mediaPlayer = MediaPlayer.create(context, R.raw.error_sound)
            // mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e("SoundManager", "Error playing error sound", e)
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}