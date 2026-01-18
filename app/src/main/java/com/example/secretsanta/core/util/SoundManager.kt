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
            // NE PAS release() immédiatement
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }

            mediaPlayer?.release()
            mediaPlayer = null

            // Crée et lance
            mediaPlayer = MediaPlayer.create(context, R.raw.jingle_bells)?.apply {
                setOnCompletionListener { player ->
                    Log.d("SoundManager", "Sound completed")
                    player.release()
                }
                setOnErrorListener { player, what, extra ->
                    Log.e("SoundManager", "MediaPlayer error: what=$what, extra=$extra")
                    player.release()
                    true
                }
                start()
                Log.d("SoundManager", "Sound playing, duration: ${duration}ms")
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error playing sound", e)
        }
    }

    // NE PAS appeler release() dans onCleared du ViewModel
    fun stopSound() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("SoundManager", "Error stopping sound", e)
        }
    }
}