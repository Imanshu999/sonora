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
            audiusApi.getTrending(
                appName = "SONORA_STREAM",
                limit = limit.coerceIn(1, 100),
                offset = 0
            ).data.map(::toTrack).take(limit)
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun search(query: String, limit: Int): List<Track> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val target = limit.coerceIn(1, 500)
        val pageSize = 100
        val output = LinkedHashMap<String, Track>()
        var offset = 0

        try {
            val maxPages = (target + pageSize - 1) / pageSize
            var pageNumber = 0
            while (pageNumber < maxPages && output.size < target) {
                val page = audiusApi.searchTracks(
                    query = query.trim(),
                    appName = "SONORA_STREAM",
                    limit = pageSize,
                    offset = offset
                ).data

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

    private fun toTrack(dto: com.example.data.remote.AudiusTrackDto): Track {
        val art = dto.artwork?.large ?: dto.artwork?.medium ?: dto.artwork?.small ?: ""
        val streamUrl = "https://discoveryprovider.audius.co/v1/tracks/${dto.id}/stream?app_name=SONORA_STREAM"
        return Track(
            id = "audius_${dto.id}",
            title = dto.title,
            artistName = dto.user?.name ?: "Unknown Artist",
            albumName = "Audius",
            durationSeconds = dto.duration ?: 180,
            audioUrl = streamUrl,
            artworkUrl = art,
            source = "Audius",
            genre = dto.genre ?: "Music"
        )
    }

    override suspend fun getStreamUrl(track: Track): String = track.audioUrl
}
