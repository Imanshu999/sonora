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
            jamendoApi.getTracks(
                clientId = clientId,
                limit = limit.coerceIn(1, 200),
                offset = 0,
                boost = "popularity_month"
            ).results.map(::toTrack).take(limit)
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun search(query: String, limit: Int): List<Track> = withContext(Dispatchers.IO) {
        if (query.isBlank() || clientId.isBlank() || clientId == "MY_JAMENDO_CLIENT_ID") {
            return@withContext emptyList()
        }

        val target = limit.coerceIn(1, 500)
        val pageSize = 100
        val output = LinkedHashMap<String, Track>()
        var offset = 0

        try {
            val maxPages = (target + pageSize - 1) / pageSize
            var pageNumber = 0
            while (pageNumber < maxPages && output.size < target) {
                val page = jamendoApi.searchTracks(
                    query = query.trim(),
                    clientId = clientId,
                    limit = pageSize,
                    offset = offset
                ).results

                if (page.isEmpty()) break
                page.map(::toTrack).forEach { output.putIfAbsent(it.id, it) }
                offset += page.size
                pageNumber++
                if (page.size < pageSize) break
            }
            output.values.take(target)
        } catch (_: Exception) {
            output.values.take(target)
        }
    }

    private fun toTrack(dto: com.example.data.remote.JamendoTrackDto): Track = Track(
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

    override suspend fun getStreamUrl(track: Track): String = track.audioUrl
}
