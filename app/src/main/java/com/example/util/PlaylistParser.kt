package com.example.util

import java.net.URI

/**
 * Parses local playlist exports only. Network playlist links are resolved by
 * ExternalPlaylistResolver so this class never scrapes third-party pages.
 */
data class ParsedTrackItem(
    val title: String,
    val artist: String = "",
    val rawQuery: String = ""
)

object PlaylistParser {
    fun parseCsv(input: String): Pair<String, List<ParsedTrackItem>> {
        val rows = CsvReader.read(input)
        if (rows.isEmpty()) return "CSV Playlist" to emptyList()

        val header = rows.first().map { it.trim().lowercase() }
        val hasHeader = header.any { it in setOf("title", "track", "track name", "song", "artist", "artist name") }
        val data = if (hasHeader) rows.drop(1) else rows

        val titleIndex = header.indexOfFirst { it in setOf("title", "track", "track name", "song") }.takeIf { it >= 0 } ?: 0
        val artistIndex = header.indexOfFirst { it in setOf("artist", "artist name") }.takeIf { it >= 0 } ?: 1

        val tracks = data.mapNotNull { columns ->
            val title = columns.getOrNull(titleIndex)?.trim().orEmpty()
            val artist = columns.getOrNull(artistIndex)?.trim().orEmpty()
            if (title.isBlank()) null else ParsedTrackItem(title, artist, "$title $artist".trim())
        }
        return "CSV Playlist" to tracks
    }

    fun isPlaylistUrl(input: String): Boolean = runCatching {
        val uri = URI(input.trim())
        uri.scheme in setOf("http", "https") && (
            uri.host?.contains("spotify.com") == true ||
                uri.host?.contains("youtube.com") == true ||
                uri.host?.contains("youtu.be") == true ||
                uri.host?.contains("music.apple.com") == true
            )
    }.getOrDefault(false)
}

private object CsvReader {
    fun read(input: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val row = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var i = 0
        while (i < input.length) {
            val c = input[i]
            when {
                c == '"' && quoted && i + 1 < input.length && input[i + 1] == '"' -> {
                    field.append('"'); i++
                }
                c == '"' -> quoted = !quoted
                c == ',' && !quoted -> { row += field.toString(); field.clear() }
                c == '\n' && !quoted -> { row += field.toString(); field.clear(); rows += row.toList(); row.clear() }
                c != '\r' -> field.append(c)
            }
            i++
        }
        row += field.toString()
        if (row.any { it.isNotBlank() }) rows += row.toList()
        return rows
    }
}
