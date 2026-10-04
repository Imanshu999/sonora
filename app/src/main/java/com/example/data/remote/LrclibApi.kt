package com.example.data.remote

import com.example.model.LyricLine
import com.example.model.SyncedLyrics
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.regex.Pattern

@JsonClass(generateAdapter = true)
data class LrclibTrackDto(
    @Json(name = "id") val id: Long?,
    @Json(name = "trackName") val trackName: String?,
    @Json(name = "artistName") val artistName: String?,
    @Json(name = "albumName") val albumName: String?,
    @Json(name = "duration") val duration: Double?,
    @Json(name = "instrumental") val instrumental: Boolean?,
    @Json(name = "plainLyrics") val plainLyrics: String?,
    @Json(name = "syncedLyrics") val syncedLyrics: String?
)

interface LrclibApi {
    @GET("api/get")
    suspend fun getLyrics(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String,
        @Query("duration") durationSec: Int? = null
    ): LrclibTrackDto

    @GET("api/search")
    suspend fun searchLyrics(
        @Query("q") query: String
    ): List<LrclibTrackDto>
}

object LrcParser {
    // Regex for LRC line like "[01:23.45] Lyric line text" or "[01:23.456] Lyric line text"
    private val LRC_REGEX = Pattern.compile("^\\[(\\d{2}):(\\d{2})(?:\\.(\\d{2,3}))?\\](.*)$")

    fun parse(lrcContent: String?, trackId: String): SyncedLyrics {
        if (lrcContent.isNullOrBlank()) {
            return SyncedLyrics(
                trackId = trackId,
                isSynced = false,
                lines = emptyList(),
                rawPlain = "No synchronized lyrics available for this song."
            )
        }

        val lines = mutableListOf<LyricLine>()
        val rawLines = lrcContent.split("\n", "\r\n")

        for (rawLine in rawLines) {
            val trimmed = rawLine.trim()
            val matcher = LRC_REGEX.matcher(trimmed)
            if (matcher.matches()) {
                val minutes = matcher.group(1)?.toLongOrNull() ?: 0L
                val seconds = matcher.group(2)?.toLongOrNull() ?: 0L
                val msPart = matcher.group(3) ?: "00"
                val millis = if (msPart.length == 2) {
                    msPart.toLongOrNull()?.times(10) ?: 0L
                } else {
                    msPart.toLongOrNull() ?: 0L
                }

                val totalTimeMs = (minutes * 60 * 1000) + (seconds * 1000) + millis
                val text = matcher.group(4)?.trim().orEmpty()
                if (text.isNotEmpty() || lines.isNotEmpty()) {
                    lines.add(LyricLine(timeMs = totalTimeMs, text = text))
                }
            }
        }

        return if (lines.isNotEmpty()) {
            SyncedLyrics(
                trackId = trackId,
                isSynced = true,
                lines = lines.sortedBy { it.timeMs },
                rawPlain = lines.joinToString("\n") { it.text }
            )
        } else {
            SyncedLyrics(
                trackId = trackId,
                isSynced = false,
                lines = emptyList(),
                rawPlain = lrcContent
            )
        }
    }
}
