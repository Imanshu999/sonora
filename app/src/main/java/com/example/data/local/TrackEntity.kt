package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Track

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artistName: String,
    val albumName: String = "",
    val durationSeconds: Int = 0,
    val audioUrl: String,
    val artworkUrl: String,
    val source: String = "Jamendo",
    val genre: String = "",
    val licenseUrl: String = "",
    val shareUrl: String = "",
    val isDownloadable: Boolean = true,
    val localUri: String? = null,
    val isLiked: Boolean = false,
    val downloadedAt: Long? = null,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long? = null
) {
    fun toTrack(): Track = Track(
        id = id,
        title = title,
        artistName = artistName,
        albumName = albumName,
        durationSeconds = durationSeconds,
        audioUrl = audioUrl,
        artworkUrl = artworkUrl,
        source = source,
        genre = genre,
        licenseUrl = licenseUrl,
        shareUrl = shareUrl,
        isDownloadable = isDownloadable,
        localUri = localUri,
        isLiked = isLiked,
        downloadedAt = downloadedAt,
        playCount = playCount,
        lastPlayedTimestamp = lastPlayedTimestamp
    )

    companion object {
        fun fromTrack(track: Track): TrackEntity = TrackEntity(
            id = track.id,
            title = track.title,
            artistName = track.artistName,
            albumName = track.albumName,
            durationSeconds = track.durationSeconds,
            audioUrl = track.audioUrl,
            artworkUrl = track.artworkUrl,
            source = track.source,
            genre = track.genre,
            licenseUrl = track.licenseUrl,
            shareUrl = track.shareUrl,
            isDownloadable = track.isDownloadable,
            localUri = track.localUri,
            isLiked = track.isLiked,
            downloadedAt = track.downloadedAt,
            playCount = track.playCount,
            lastPlayedTimestamp = track.lastPlayedTimestamp
        )
    }
}
