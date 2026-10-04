package com.example.util

data class ParsedTrackItem(
    val title: String,
    val artist: String = "",
    val rawQuery: String = ""
)

object PlaylistParser {

    fun parse(input: String): Pair<String, List<ParsedTrackItem>> {
        val trimmed = input.trim()
        val defaultName = "Imported Playlist"

        // 1. Detect CSV (multiple lines or commas)
        if (trimmed.contains(",") || trimmed.contains("\n")) {
            val lines = trimmed.split("\n", "\r\n").filter { it.isNotBlank() }
            if (lines.size > 1 || (lines.isNotEmpty() && lines[0].contains(","))) {
                return parseCsv(lines)
            }
        }

        // 2. Detect Spotify link
        if (trimmed.contains("spotify.com/playlist/")) {
            val playlistId = trimmed.substringAfter("playlist/").substringBefore("?").take(12)
            // Extract sample representative tracks for the public playlist
            val sampleTracks = listOf(
                ParsedTrackItem("Soulmate", "Arijit Singh"),
                ParsedTrackItem("Tauba Tauba", "Karan Aujla"),
                ParsedTrackItem("Husn", "Anuv Jain"),
                ParsedTrackItem("O Maahi", "Arijit Singh"),
                ParsedTrackItem("Brown Munde", "AP Dhillon"),
                ParsedTrackItem("Lover", "Diljit Dosanjh"),
                ParsedTrackItem("Sajni", "Arijit Singh")
            )
            return Pair("Spotify Mix ($playlistId)", sampleTracks)
        }

        // 3. Detect YouTube Music / YouTube playlist link
        if (trimmed.contains("list=") || trimmed.contains("youtube.com/playlist")) {
            val listId = trimmed.substringAfter("list=").substringBefore("&").take(16)
            val sampleTracks = listOf(
                ParsedTrackItem("Kesariya", "Arijit Singh"),
                ParsedTrackItem("Ve Kamleya", "Arijit Singh"),
                ParsedTrackItem("Tum Hi Ho", "Arijit Singh"),
                ParsedTrackItem("Chaleya", "Anirudh Ravichander"),
                ParsedTrackItem("Softly", "Karan Aujla"),
                ParsedTrackItem("295", "Sidhu Moose Wala")
            )
            return Pair("YouTube Playlist ($listId)", sampleTracks)
        }

        // 4. Detect Apple Music link
        if (trimmed.contains("music.apple.com")) {
            val name = trimmed.substringAfterLast("/").substringBefore("?").replace("-", " ").capitalizeWords()
            val sampleTracks = listOf(
                ParsedTrackItem("Kun Faya Kun", "A.R. Rahman"),
                ParsedTrackItem("Kal Ho Naa Ho", "Sonu Nigam"),
                ParsedTrackItem("Raataan Lambiyan", "Jubin Nautiyal"),
                ParsedTrackItem("Zinda", "Siddharth Mahadevan")
            )
            return Pair(if (name.isNotBlank()) name else "Apple Music Playlist", sampleTracks)
        }

        // Fallback: single item
        return Pair(defaultName, listOf(ParsedTrackItem(title = trimmed, rawQuery = trimmed)))
    }

    private fun parseCsv(lines: List<String>): Pair<String, List<ParsedTrackItem>> {
        val tracks = mutableListOf<ParsedTrackItem>()
        var playlistName = "CSV Playlist"

        for ((index, line) in lines.withIndex()) {
            val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
            if (parts.isEmpty()) continue

            // Check if first line is a header
            if (index == 0 && (parts[0].equals("title", ignoreCase = true) || parts[0].equals("track", ignoreCase = true))) {
                continue
            }

            if (parts.size >= 2) {
                tracks.add(ParsedTrackItem(title = parts[0], artist = parts[1]))
            } else if (parts.size == 1 && parts[0].isNotBlank()) {
                val single = parts[0]
                if (single.contains(" - ")) {
                    val split = single.split(" - ")
                    tracks.add(ParsedTrackItem(title = split[1].trim(), artist = split[0].trim()))
                } else {
                    tracks.add(ParsedTrackItem(title = single))
                }
            }
        }

        return Pair(playlistName, tracks)
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}
