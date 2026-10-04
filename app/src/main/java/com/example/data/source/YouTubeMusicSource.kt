package com.example.data.source

import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class YouTubeMusicSource : MusicSource {

    override val sourceId: String = "YOUTUBE"
    override val displayName: String = "YouTube Music"

    override suspend fun getTrending(limit: Int): List<Track> = withContext(Dispatchers.IO) {
        YouTubeMusicCatalog.trendingIndia.take(limit)
    }

    override suspend fun search(query: String, limit: Int): List<Track> = withContext(Dispatchers.IO) {
        val q = query.trim().lowercase()
        YouTubeMusicCatalog.allTracks.filter {
            it.title.lowercase().contains(q) ||
            it.artistName.lowercase().contains(q) ||
            it.albumName.lowercase().contains(q) ||
            it.genre.lowercase().contains(q)
        }.take(limit)
    }

    override suspend fun getStreamUrl(track: Track): String {
        return track.audioUrl
    }
}
