package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Playlist

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toPlaylist(trackCount: Int = 0, coverArtworkUrl: String? = null): Playlist = Playlist(
        id = id,
        name = name,
        description = description,
        createdAt = createdAt,
        trackCount = trackCount,
        coverArtworkUrl = coverArtworkUrl
    )
}

@Entity(tableName = "playlist_tracks", primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrackCrossRef(
    val playlistId: Long,
    val trackId: String,
    val addedAt: Long = System.currentTimeMillis()
)
