package com.example.data.local

import androidx.room.Entity

@Entity(tableName = "home_rows", primaryKeys = ["rowId", "trackId"])
data class HomeRowTrackEntity(
    val rowId: String,
    val trackId: String,
    val rowTitle: String,
    val rowSubtitle: String = "",
    val languageRegion: String = "ALL",
    val sortOrder: Int = 0
)
