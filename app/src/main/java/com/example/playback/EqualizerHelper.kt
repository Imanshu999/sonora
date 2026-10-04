package com.example.playback

import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.util.Log
import com.example.model.EqualizerPreset

class EqualizerHelper {
    private var equalizer: Equalizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var currentAudioSessionId: Int = 0

    fun bindAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0 || audioSessionId == currentAudioSessionId) return
        release()
        currentAudioSessionId = audioSessionId

        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.w("EqualizerHelper", "Failed to init Equalizer: ${e.message}")
        }

        try {
            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                enabled = true
                setTargetGain(150) // subtle loudness boost
            }
        } catch (e: Exception) {
            Log.w("EqualizerHelper", "Failed to init LoudnessEnhancer: ${e.message}")
        }
    }

    fun applyPreset(preset: EqualizerPreset) {
        val eq = equalizer ?: return
        try {
            val numBands = eq.numberOfBands.toInt()
            val minLevel = eq.bandLevelRange[0]
            val maxLevel = eq.bandLevelRange[1]
            val range = (maxLevel - minLevel).toFloat()

            for (i in 0 until numBands) {
                val gainNorm = preset.gains.getOrElse(i) { 0 }
                // Map from -5..+6 to actual millibels
                val targetLevel = (gainNorm / 10f * (range / 2f)).toInt().toShort()
                    .coerceIn(minLevel, maxLevel)
                eq.setBandLevel(i.toShort(), targetLevel)
            }
        } catch (e: Exception) {
            Log.w("EqualizerHelper", "Error applying preset: ${e.message}")
        }
    }

    fun setLoudnessEnabled(enabled: Boolean) {
        try {
            loudnessEnhancer?.enabled = enabled
        } catch (e: Exception) {
            Log.w("EqualizerHelper", "Error toggling loudness: ${e.message}")
        }
    }

    fun release() {
        try {
            equalizer?.release()
        } catch (_: Exception) {}
        equalizer = null

        try {
            loudnessEnhancer?.release()
        } catch (_: Exception) {}
        loudnessEnhancer = null
        currentAudioSessionId = 0
    }
}
