package com.example.data.source

import com.example.data.remote.JamendoApi
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JamendoSource(
    private val jamendoApi: JamendoApi,
    private val clientId: String
) : MusicSource {

    override val sourceId: String = "JAMENDO"
    override val displayName: String = "Jamendo (CC)"

    override suspend fun getTrending(limit: Int): List<Track> = withContext(Dispatchers.IO) {
        try {
            val response = jamendoApi.getTracks(clientId = clientId, limit = limit, boost = "popularity_month")
            val tracks = response.results.map { dto ->
                Track(
                    id = "jamendo_${dto.id}",
                    title = dto.name,
                    artistName = dto.artistName,
                    albumName = dto.albumName ?: "Single",
                    durationSeconds = dto.duration ?: 180,
                    audioUrl = dto.audio,
                    artworkUrl = dto.image ?: "",
                    source = "Jamendo",
                    licenseUrl = dto.licenseCcUrl ?: "https://creativecommons.org/licenses/by/4.0/",
                    shareUrl = dto.shareurl ?: ""
                )
            }
            return@withContext tracks
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun search(query: String, limit: Int): List<Track> = withContext(Dispatchers.IO) {
        try {
            val response = jamendoApi.searchTracks(query = query, clientId = clientId, limit = limit)
            val tracks = response.results.map { dto ->
                Track(
                    id = "jamendo_${dto.id}",
                    title = dto.name,
                    artistName = dto.artistName,
                    albumName = dto.albumName ?: "",
                    durationSeconds = dto.duration ?: 180,
                    audioUrl = dto.audio,
                    artworkUrl = dto.image ?: "",
                    source = "Jamendo"
                )
            }
            return@withContext tracks
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getStreamUrl(track: Track): String {
        return track.audioUrl
    }
}
