package com.example.data.source

import com.example.model.Track

/**
 * YouTube Music is intentionally not backed by a fake/static catalog.
 * The original InnerTube/NewPipeExtractor implementation is not present in this fork,
 * so this source stays empty until that existing real data layer is restored.
 */
class YouTubeMusicSource : MusicSource {
    override val sourceId: String = "YOUTUBE"
    override val displayName: String = "YouTube Music"

    override suspend fun getTrending(limit: Int): List<Track> = emptyList()

    override suspend fun search(query: String, limit: Int): List<Track> = emptyList()

    override suspend fun getStreamUrl(track: Track): String = track.audioUrl
}
