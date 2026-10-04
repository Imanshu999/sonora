package com.example.model

data class LyricLine(
    val timeMs: Long,
    val text: String
)

data class SyncedLyrics(
    val trackId: String,
    val isSynced: Boolean,
    val lines: List<LyricLine>,
    val rawPlain: String = ""
)
