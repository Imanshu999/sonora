package com.example.data.source

import com.example.data.remote.FreeToUseApi
import com.example.data.remote.FreeToUseTrackDto
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Adapter for the FreeToUse music catalog.
 * Network failures are intentionally converted to an empty result so one
 * optional provider cannot prevent the rest of Sonora from loading.
 */
class FreeToUseSource(
    private val api: FreeToUseApi
) : MusicSource {

    override val sourceId: String = "FREE_TO_USE"
    override val displayName: String = "FreeToUse"

    override suspend fun getTrending(limit: Int): List<Track> = withContext(Dispatchers.IO) {
        runCatching {
            api.getAllTracks(limit = limit.coerceIn(1, 100), offset = 0)
                .data
                .orEmpty()
                .mapNotNull { it.toTrack() }
        }.getOrDefault(emptyList())
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
                val response = api.searchTracks(
                    query = query.trim(),
                    limit = pageSize,
                    offset = offset
                )
                val page = response.data.orEmpty()
                if (page.isEmpty()) break
                page.mapNotNull { it.toTrack() }.forEach { output.putIfAbsent(it.id, it) }
                offset += page.size
                pageNumber++
                if (page.size < pageSize) break
            }
            output.values.take(target)
        } catch (_: Exception) {
            output.values.take(target)
        }
    }

    override suspend fun getStreamUrl(track: Track): String = track.audioUrl

    private fun FreeToUseTrackDto.toTrack(): Track? {
        val streamUrl = files?.mp3?.takeIf { it.isNotBlank() } ?: return null
        val artwork = thumbnails?.xl
            ?: thumbnails?.lg
            ?: thumbnails?.md
            ?: thumbnails?.sm
            ?: ""

        val artist = artists
            .asSequence()
            .mapNotNull { row ->
                row.firstOrNull()?.let { value ->
                    value.toString().trim('"')
                }
            }
            .filter { it.isNotBlank() }
            .joinToString(", ")
            .ifBlank { "Unknown Artist" }

        return Track(
            id = "free_to_use_$id",
            title = title.ifBlank { "Untitled" },
            artistName = artist,
            albumName = record_label.orEmpty(),
            durationSeconds = duration.toInt().coerceAtLeast(0),
            audioUrl = streamUrl,
            artworkUrl = artwork,
            source = "FreeToUse",
            genre = genre.orEmpty(),
            isDownloadable = !is_premium
        )
    }
}
