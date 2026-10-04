package com.example.data.source

import com.example.model.Track

interface MusicSource {
    val sourceId: String
    val displayName: String

    suspend fun search(query: String, limit: Int = 25): List<Track>
    suspend fun getTrending(limit: Int = 25): List<Track>
    suspend fun getStreamUrl(track: Track): String
}
