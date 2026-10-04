package com.example.model

data class HomeRow(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val tracks: List<Track> = emptyList(),
    val languageRegion: String = "ALL" // "ALL", "HINDI", "PUNJABI", "ENGLISH"
)

enum class MusicLanguage(val title: String, val code: String) {
    ALL("All Languages", "ALL"),
    HINDI("Hindi & Bollywood", "HINDI"),
    PUNJABI("Punjabi Hits", "PUNJABI"),
    ENGLISH("Global / English", "ENGLISH")
}
