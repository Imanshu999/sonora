package com.example.model

data class HomeRow(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val tracks: List<Track> = emptyList(),
    val languageRegion: String = "ALL" // "ALL", "HINDI", "PUNJABI", "ENGLISH"
)

data class HomeRowConfig(
    val id: String,
    val title: String,
    val subtitle: String,
    val languageRegion: String,
    val query: String? = null,
    val queries: List<String> = emptyList(),
    val catalogKey: String? = null
)

object HomeRowCatalog {
    // Add a HomeRowConfig here to make a new row appear everywhere automatically.
    val rows = listOf(
        HomeRowConfig("trending_india", "Trending in India", "Top chartbusters right now", "ALL", catalogKey = "trendingIndia"),
        HomeRowConfig("new_hindi", "New Hindi Releases", "Fresh Bollywood and indie Hindi tracks", "HINDI", query = "Hindi" , catalogKey = "newHindiReleases"),
        HomeRowConfig("punjabi_hits", "Punjabi Hits", "Bhangra, pop, and Punjabi vibes", "PUNJABI", query = "Punjabi", catalogKey = "punjabiHits"),
        HomeRowConfig("bollywood_classics", "Bollywood Classics", "Timeless Hindi melodies", "HINDI", query = "Bollywood", catalogKey = "bollywoodClassics"),
        HomeRowConfig("romantic", "Romantic", "Heartfelt tunes and love anthems", "ALL", query = "Romantic", catalogKey = "romanticTracks"),
        HomeRowConfig("workout", "Workout", "High-energy tracks for training", "ALL", query = "Workout", catalogKey = "workoutTracks"),
        HomeRowConfig("lofi", "Lo-fi", "Chill study and late-night beats", "ALL", query = "Lo-fi", catalogKey = "lofiTracks"),
        HomeRowConfig("devotional", "Devotional", "Spiritual peace and sacred chants", "HINDI", query = "Devotional", catalogKey = "devotionalTracks"),
        HomeRowConfig("top_artists", "Top Artists", "Popular artists and discoveries", "ALL", queries = listOf("Arijit Singh", "Diljit Dosanjh", "Shreya Ghoshal", "Karan Aujla"))
    )
}

enum class MusicLanguage(val title: String, val code: String) {
    ALL("All Languages", "ALL"),
    HINDI("Hindi & Bollywood", "HINDI"),
    PUNJABI("Punjabi Hits", "PUNJABI"),
    ENGLISH("Global / English", "ENGLISH")
}
