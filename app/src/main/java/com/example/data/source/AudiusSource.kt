package com.example.data.source

import com.example.data.remote.AudiusApi
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudiusSource(
    private val audiusApi: AudiusApi
) : MusicSource {

    override val sourceId: String = "AUDIUS"
    override val displayName: String = "Audius"

    override suspend fun getTrending(limit: Int): List<Track> = withContext(Dispatchers.IO) {
        try {
            val response = audiusApi.getTrending(appName = "SONORA_STREAM")
            response.data.take(limit).mapNotNull { dto ->
                val art = dto.artwork?.large ?: dto.artwork?.medium ?: dto.artwork?.small
                    ?: ""
                val streamUrl = "https://discoveryprovider.audius.co/v1/tracks/${dto.id}/stream?app_name=SONORA_STREAM"
                Track(
                    id = "audius_${dto.id}",
                    title = dto.title,
                    artistName = dto.user?.name ?: "Unknown Artist",
                    albumName = "Audius Discovery",
                    durationSeconds = dto.duration ?: 180,
                    audioUrl = streamUrl,
                    artworkUrl = art,
                    source = "Audius",
                    genre = dto.genre ?: "Music"
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun search(query: String, limit: Int): List<Track> = withContext(Dispatchers.IO) {
        try {
            val response = audiusApi.searchTracks(query = query, appName = "SONORA_STREAM")
            response.data.take(limit).mapNotNull { dto ->
                val art = dto.artwork?.large ?: dto.artwork?.medium ?: dto.artwork?.small
                    ?: ""
                val streamUrl = "https://discoveryprovider.audius.co/v1/tracks/${dto.id}/stream?app_name=SONORA_STREAM"
                Track(
                    id = "audius_${dto.id}",
                    title = dto.title,
                    artistName = dto.user?.name ?: "Unknown Artist",
                    albumName = "Audius Search",
                    durationSeconds = dto.duration ?: 180,
                    audioUrl = streamUrl,
                    artworkUrl = art,
                    source = "Audius",
                    genre = dto.genre ?: ""
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getStreamUrl(track: Track): String {
        return track.audioUrl
    }
}
