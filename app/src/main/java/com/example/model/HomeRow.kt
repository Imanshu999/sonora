package com.example.model

data class HomeRow(
    val id: String,
    val title: String,
    val subtitle: String,
    val tracks: List<Track>,
    val languageRegion: String = "ALL"
)

data class HomeRowConfig(
    val id: String,
    val title: String,
    val subtitle: String,
    val languageRegion: String = "ALL",
    val query: String? = null,
    val queries: List<String> = emptyList(),
    val useTrending: Boolean = false
)

/** Single source of truth for Home/Discover rows. Add a HomeRowConfig to add a row. */
object HomeRowCatalog {
    val rows = listOf(
        HomeRowConfig("worldwide_trending", "Trending Worldwide", "Real-time discoveries from enabled music sources", useTrending = true),
        HomeRowConfig("new_releases", "New & Popular", "Fresh tracks from the global catalog", query = "new music"),
        HomeRowConfig("pop", "Pop", "Popular music from around the world", query = "pop"),
        HomeRowConfig("romantic", "Romantic", "Love songs and mellow discoveries", query = "romantic"),
        HomeRowConfig("workout", "Workout", "High-energy music for training", query = "workout"),
        HomeRowConfig("lofi", "Lo-fi", "Chill, study and late-night beats", query = "lofi"),
        HomeRowConfig("devotional", "Devotional", "Spiritual and devotional music", query = "devotional"),
        HomeRowConfig("rock", "Rock", "Rock discoveries across countries", query = "rock"),
        HomeRowConfig("electronic", "Electronic", "Electronic and dance discoveries", query = "electronic"),
        HomeRowConfig("jazz", "Jazz", "Jazz artists and new discoveries", query = "jazz"),
        HomeRowConfig("top_artists", "Top Artists", "Popular artists surfaced by the enabled sources", useTrending = true)
    )
}

enum class MusicLanguage(val title: String, val code: String) {
    ALL("All Languages", "ALL"),
    HINDI("Hindi", "HINDI"),
    PUNJABI("Punjabi", "PUNJABI"),
    ENGLISH("English", "ENGLISH"),
    SPANISH("Spanish", "SPANISH"),
    FRENCH("French", "FRENCH"),
    GERMAN("German", "GERMAN"),
    KOREAN("Korean", "KOREAN"),
    JAPANESE("Japanese", "JAPANESE"),
    ARABIC("Arabic", "ARABIC"),
    PORTUGUESE("Portuguese", "PORTUGUESE"),
    OTHER("Other Languages", "OTHER")
}
