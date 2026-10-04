package com.example.model

data class Track(
    val id: String,
    val title: String,
    val artistName: String,
    val albumName: String = "",
    val durationSeconds: Int = 0,
    val audioUrl: String,
    val artworkUrl: String,
    val source: String = "",
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
    val isDownloaded: Boolean
        get() = localUri != null

    val playbackUri: String
        get() = localUri ?: audioUrl

    val formattedDuration: String
        get() {
            if (durationSeconds <= 0) return "0:00"
            val m = durationSeconds / 60
            val s = durationSeconds % 60
            return "%d:%02d".format(m, s)
        }
}
