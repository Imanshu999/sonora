package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AudioQuality
import com.example.model.EqualizerPreset
import com.example.model.SonoraTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sonora_preferences")

class DataStoreManager(private val context: Context) {

    companion object {
        val KEY_THEME = stringPreferencesKey("theme_mode")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val KEY_AUDIO_QUALITY = stringPreferencesKey("audio_quality")
        val KEY_CROSSFADE = intPreferencesKey("crossfade_seconds")
        val KEY_EQ_PRESET = stringPreferencesKey("eq_preset")
        val KEY_LOUDNESS_NORM = booleanPreferencesKey("loudness_norm")
        val KEY_LANGUAGE = stringPreferencesKey("music_language")
        val KEY_PREFERRED_SOURCE = stringPreferencesKey("preferred_source")
    }

    val languageFlow: Flow<com.example.model.MusicLanguage> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_LANGUAGE] ?: com.example.model.MusicLanguage.ALL.name
        try {
            com.example.model.MusicLanguage.valueOf(name)
        } catch (_: Exception) {
            com.example.model.MusicLanguage.ALL
        }
    }

    val preferredSourcesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_PREFERRED_SOURCE] ?: "ALL"
        if (raw == "ALL") setOf("YOUTUBE", "AUDIUS", "JAMENDO", "FREE_TO_USE")
        else raw.split(',').map { it.trim() }.filter { it.isNotBlank() }.toSet()
    }

    val preferredSourceFlow: Flow<String> = preferredSourcesFlow.map { sources ->
        if (sources.size == 3) "ALL" else sources.sorted().joinToString(",")
    }

    val themeFlow: Flow<SonoraTheme> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_THEME] ?: SonoraTheme.DARK.name
        try {
            SonoraTheme.valueOf(name)
        } catch (_: Exception) {
            SonoraTheme.DARK
        }
    }

    val dynamicColorFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DYNAMIC_COLOR] ?: false
    }

    val audioQualityFlow: Flow<AudioQuality> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_AUDIO_QUALITY] ?: AudioQuality.HIGH.name
        try {
            AudioQuality.valueOf(name)
        } catch (_: Exception) {
            AudioQuality.HIGH
        }
    }

    val crossfadeSecondsFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_CROSSFADE] ?: 2
    }

    val equalizerPresetFlow: Flow<EqualizerPreset> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_EQ_PRESET] ?: EqualizerPreset.FLAT.name
        try {
            EqualizerPreset.valueOf(name)
        } catch (_: Exception) {
            EqualizerPreset.FLAT
        }
    }

    val loudnessNormalizationFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_LOUDNESS_NORM] ?: true
    }

    suspend fun setTheme(theme: SonoraTheme) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setAudioQuality(quality: AudioQuality) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUDIO_QUALITY] = quality.name
        }
    }

    suspend fun setCrossfade(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CROSSFADE] = seconds
        }
    }

    suspend fun setEqualizerPreset(preset: EqualizerPreset) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EQ_PRESET] = preset.name
        }
    }

    suspend fun setLoudnessNormalization(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LOUDNESS_NORM] = enabled
        }
    }

    suspend fun setLanguage(language: com.example.model.MusicLanguage) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language.name
        }
    }

    suspend fun setPreferredSource(source: String) {
        val sources = when (source) {
            "ALL" -> setOf("YOUTUBE", "AUDIUS", "JAMENDO", "FREE_TO_USE")
            else -> setOf(source)
        }
        setPreferredSources(sources)
    }

    suspend fun setPreferredSources(sources: Set<String>) {
        val normalized = sources.intersect(setOf("YOUTUBE", "AUDIUS", "JAMENDO", "FREE_TO_USE"))
        context.dataStore.edit { prefs ->
            prefs[KEY_PREFERRED_SOURCE] = if (normalized.size == 3) "ALL" else normalized.sorted().joinToString(",")
        }
    }
}
