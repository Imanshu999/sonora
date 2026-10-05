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
        HomeRowConfig("india_trending", "🇮🇳 India Trending", "Fresh Indian music from enabled catalogs", languageRegion = "INDIA", queries = listOf("Indian music", "India trending", "Hindi songs", "Punjabi songs")),
        HomeRowConfig("hindi_hits", "Hindi Hits", "Hindi songs and Bollywood discoveries", languageRegion = "INDIA", queries = listOf("Hindi songs", "Bollywood", "Hindi pop", "Hindi romantic")),
        HomeRowConfig("punjabi_hits", "Punjabi Hits", "Punjabi songs and artists", languageRegion = "INDIA", queries = listOf("Punjabi songs", "Punjabi pop", "Punjabi hip hop")),
        HomeRowConfig("tamil_hits", "Tamil Music", "Tamil songs and artists", languageRegion = "INDIA", queries = listOf("Tamil songs", "Tamil music")),
        HomeRowConfig("telugu_hits", "Telugu Music", "Telugu songs and artists", languageRegion = "INDIA", queries = listOf("Telugu songs", "Telugu music")),
        HomeRowConfig("bengali_hits", "Bengali Music", "Bengali songs and artists", languageRegion = "INDIA", queries = listOf("Bengali songs", "Bengali music")),
        HomeRowConfig("marathi_hits", "Marathi Music", "Marathi songs and artists", languageRegion = "INDIA", queries = listOf("Marathi songs", "Marathi music")),
        HomeRowConfig("indian_devotional", "Indian Devotional", "Bhajan, devotional and spiritual music", languageRegion = "INDIA", queries = listOf("Indian devotional", "bhajan", "Hindu devotional")),
        HomeRowConfig("worldwide_trending", "Trending Worldwide", "Real-time discoveries from enabled music sources", useTrending = true),
        HomeRowConfig("new_releases", "New & Popular", "Fresh tracks from the global catalog", query = "new music"),
        HomeRowConfig("pop", "Global Pop", "Popular music from around the world", query = "pop"),
        HomeRowConfig("romantic", "Romantic", "Love songs and mellow discoveries", query = "romantic"),
        HomeRowConfig("workout", "Workout", "High-energy music for training", query = "workout"),
        HomeRowConfig("lofi", "Lo-fi", "Chill, study and late-night beats", query = "lofi"),
        HomeRowConfig("rock", "Rock", "Rock discoveries across countries", query = "rock"),
        HomeRowConfig("electronic", "Electronic", "Electronic and dance discoveries", query = "electronic"),
        HomeRowConfig(
            "phonk",
            "🔥 Phonk",
            "Dedicated Phonk hub — drift, Brazilian, Memphis, wave, house and underground Phonk",
            queries = listOf(
                "phonk",
                "phonk music",
                "drift phonk",
                "Brazilian phonk",
                "Memphis phonk",
                "wave phonk",
                "house phonk",
                "aggressive phonk",
                "atmospheric phonk",
                "underground phonk",
                "cowbell phonk",
                "phonk funk"
            )
        ),
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
