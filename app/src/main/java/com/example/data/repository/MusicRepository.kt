package com.example.data.repository

import com.example.data.local.HomeRowTrackEntity
import com.example.data.local.OfflineDownloader
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistTrackCrossRef
import com.example.data.local.SonoraDao
import com.example.data.local.TrackEntity
import com.example.data.remote.AudiusApi
import com.example.data.remote.CuratedCatalog
import com.example.data.remote.JamendoApi
import com.example.data.remote.LrcParser
import com.example.data.remote.LrclibApi
import com.example.data.source.AudiusSource
import com.example.data.source.JamendoSource
import com.example.data.source.MusicSource
import com.example.data.source.YouTubeMusicCatalog
import com.example.data.source.YouTubeMusicSource
import com.example.model.HomeRow
import com.example.model.MusicLanguage
import com.example.model.Playlist
import com.example.model.SyncedLyrics
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MusicRepository(
    private val jamendoApi: JamendoApi,
    private val audiusApi: AudiusApi,
    private val lrclibApi: LrclibApi,
    private val dao: SonoraDao,
    private val downloader: OfflineDownloader
) {

    val youTubeSource = YouTubeMusicSource()
    val audiusSource = AudiusSource(audiusApi)
    val jamendoSource = JamendoSource(jamendoApi)

    val sources: List<MusicSource> = listOf(youTubeSource, audiusSource, jamendoSource)

    val likedTracks: Flow<List<Track>> = dao.getLikedTracks().map { list -> list.map { it.toTrack() } }
    val downloadedTracks: Flow<List<Track>> = dao.getDownloadedTracks().map { list -> list.map { it.toTrack() } }
    val listeningHistory: Flow<List<Track>> = dao.getListeningHistory(30).map { list -> list.map { it.toTrack() } }

    val playlists: Flow<List<Playlist>> = dao.getAllPlaylists().map { entities ->
        entities.map { entity ->
            val count = dao.getPlaylistTrackCount(entity.id)
            val cover = dao.getPlaylistCoverUrl(entity.id)
            entity.toPlaylist(trackCount = count, coverArtworkUrl = cover)
        }
    }

    suspend fun getHomeRows(
        language: MusicLanguage = MusicLanguage.ALL,
        sourceFilter: String = "ALL",
        forceRefresh: Boolean = false
    ): List<HomeRow> = withContext(Dispatchers.IO) {
        // 1. Try reading from Room cache first if not force refresh
        if (!forceRefresh) {
            val cachedEntities = dao.getAllCachedHomeRows().firstOrNull() ?: emptyList()
            if (cachedEntities.isNotEmpty()) {
                val grouped = cachedEntities.groupBy { it.rowId }
                val cachedRows = grouped.mapNotNull { (rowId, entities) ->
                    val first = entities.first()
                    val tracks = entities.mapNotNull { e -> dao.getTrackById(e.trackId)?.toTrack() }
                    if (tracks.isNotEmpty()) {
                        HomeRow(
                            id = rowId,
                            title = first.rowTitle,
                            subtitle = first.rowSubtitle,
                            tracks = tracks,
                            languageRegion = first.languageRegion
                        )
                    } else null
                }
                if (cachedRows.isNotEmpty()) {
                    return@withContext filterRowsByLanguage(cachedRows, language)
                }
            }
        }

        // 2. Fetch fresh audius trending for dynamic global blend
        val audiusTracks = try {
            audiusSource.getTrending(8)
        } catch (_: Exception) {
            emptyList()
        }

        // 3. Construct single configurable list of home rows
        val allRows = listOf(
            HomeRow(
                id = "trending_india",
                title = "Trending in India",
                subtitle = "Top chartbusters right now",
                tracks = YouTubeMusicCatalog.trendingIndia,
                languageRegion = "ALL"
            ),
            HomeRow(
                id = "new_hindi",
                title = "New Hindi Releases",
                subtitle = "Fresh Bollywood and indie Hindi tracks",
                tracks = YouTubeMusicCatalog.newHindiReleases,
                languageRegion = "HINDI"
            ),
            HomeRow(
                id = "punjabi_hits",
                title = "Punjabi Hits",
                subtitle = "Bhangra, pop, and Punjabi vibes",
                tracks = YouTubeMusicCatalog.punjabiHits,
                languageRegion = "PUNJABI"
            ),
            HomeRow(
                id = "bollywood_classics",
                title = "Bollywood Classics",
                subtitle = "Timeless melodies from golden eras",
                tracks = YouTubeMusicCatalog.bollywoodClassics,
                languageRegion = "HINDI"
            ),
            HomeRow(
                id = "romantic",
                title = "Romantic",
                subtitle = "Heartfelt tunes and love anthems",
                tracks = YouTubeMusicCatalog.romanticTracks,
                languageRegion = "ALL"
            ),
            HomeRow(
                id = "workout",
                title = "Workout",
                subtitle = "High-octane energetic workout tracks",
                tracks = YouTubeMusicCatalog.workoutTracks,
                languageRegion = "ALL"
            ),
            HomeRow(
                id = "lofi",
                title = "Lo-fi",
                subtitle = "Chill late night study & chill beats",
                tracks = YouTubeMusicCatalog.lofiTracks,
                languageRegion = "ALL"
            ),
            HomeRow(
                id = "devotional",
                title = "Devotional",
                subtitle = "Spiritual peace and sacred chants",
                tracks = YouTubeMusicCatalog.devotionalTracks,
                languageRegion = "HINDI"
            ),
            HomeRow(
                id = "top_artists",
                title = "Top Artists Spotlight",
                subtitle = "Arijit Singh, Diljit Dosanjh, Shreya Ghoshal & more",
                tracks = (YouTubeMusicCatalog.allTracks.shuffled()).take(6),
                languageRegion = "ALL"
            ),
            HomeRow(
                id = "audius_global",
                title = "Audius Global Discoveries",
                subtitle = "Decentralized trending worldwide",
                tracks = audiusTracks.ifEmpty { CuratedCatalog.tracks.take(6) },
                languageRegion = "ENGLISH"
            )
        )

        // 4. Save to Room database for offline-first persistence
        try {
            dao.clearHomeRows()
            val entitiesToInsert = mutableListOf<HomeRowTrackEntity>()
            val tracksToInsert = mutableListOf<TrackEntity>()

            for (row in allRows) {
                row.tracks.forEachIndexed { index, track ->
                    tracksToInsert.add(TrackEntity.fromTrack(track))
                    entitiesToInsert.add(
                        HomeRowTrackEntity(
                            rowId = row.id,
                            trackId = track.id,
                            rowTitle = row.title,
                            rowSubtitle = row.subtitle,
                            languageRegion = row.languageRegion,
                            sortOrder = index
                        )
                    )
                }
            }
            dao.upsertTracks(tracksToInsert)
            dao.insertHomeRowTracks(entitiesToInsert)
        } catch (_: Exception) {}

        filterRowsByLanguage(allRows, language)
    }

    private fun filterRowsByLanguage(rows: List<HomeRow>, language: MusicLanguage): List<HomeRow> {
        return when (language) {
            MusicLanguage.ALL -> rows
            MusicLanguage.HINDI -> {
                rows.sortedByDescending { it.languageRegion == "HINDI" }
                    .filter { it.languageRegion == "ALL" || it.languageRegion == "HINDI" }
            }
            MusicLanguage.PUNJABI -> {
                rows.sortedByDescending { it.languageRegion == "PUNJABI" }
                    .filter { it.languageRegion == "ALL" || it.languageRegion == "PUNJABI" }
            }
            MusicLanguage.ENGLISH -> {
                rows.sortedByDescending { it.languageRegion == "ENGLISH" }
                    .filter { it.languageRegion == "ALL" || it.languageRegion == "ENGLISH" }
            }
        }
    }

    suspend fun getTrendingTracks(): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        tracks.addAll(YouTubeMusicCatalog.trendingIndia)
        try {
            val audiusResp = audiusSource.getTrending(6)
            tracks.addAll(audiusResp)
        } catch (_: Exception) {}
        syncWithDatabase(tracks)
    }

    suspend fun getNewReleases(): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        tracks.addAll(YouTubeMusicCatalog.newHindiReleases)
        tracks.addAll(YouTubeMusicCatalog.punjabiHits)
        syncWithDatabase(tracks)
    }

    suspend fun getTracksByGenre(genre: String): List<Track> = withContext(Dispatchers.IO) {
        val local = YouTubeMusicCatalog.allTracks.filter { it.genre.contains(genre, ignoreCase = true) }
        if (local.isNotEmpty()) {
            return@withContext syncWithDatabase(local)
        }
        try {
            val audius = audiusSource.search(genre, 10)
            if (audius.isNotEmpty()) return@withContext syncWithDatabase(audius)
        } catch (_: Exception) {}
        syncWithDatabase(CuratedCatalog.tracks.filter { it.genre.contains(genre, ignoreCase = true) }.ifEmpty { CuratedCatalog.tracks })
    }

    suspend fun search(query: String, sourceFilter: String = "ALL"): List<Track> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val results = mutableListOf<Track>()

        // 1. YouTube Music search
        if (sourceFilter == "ALL" || sourceFilter == "YOUTUBE") {
            try {
                results.addAll(youTubeSource.search(query))
            } catch (_: Exception) {}
        }

        // 2. Audius search
        if (sourceFilter == "ALL" || sourceFilter == "AUDIUS") {
            try {
                results.addAll(audiusSource.search(query))
            } catch (_: Exception) {}
        }

        // 3. Jamendo search
        if (sourceFilter == "ALL" || sourceFilter == "JAMENDO") {
            try {
                results.addAll(jamendoSource.search(query))
            } catch (_: Exception) {}
        }

        // Fallback matching if empty
        if (results.isEmpty()) {
            val fallbackMatches = (YouTubeMusicCatalog.allTracks + CuratedCatalog.tracks).filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artistName.contains(query, ignoreCase = true)
            }
            results.addAll(fallbackMatches)
        }

        syncWithDatabase(results.distinctBy { it.id })
    }

    suspend fun getSyncedLyrics(track: Track): SyncedLyrics = withContext(Dispatchers.IO) {
        try {
            val cleanTitle = track.title.replace("\\(.*?\\)|\\[.*?\\]".toRegex(), "").trim()
            val dto = lrclibApi.getLyrics(trackName = cleanTitle, artistName = track.artistName, durationSec = track.durationSeconds)
            if (!dto.syncedLyrics.isNullOrBlank()) {
                val parsed = LrcParser.parse(dto.syncedLyrics, track.id)
                if (parsed.lines.isNotEmpty()) return@withContext parsed
            }
        } catch (_: Exception) {
            try {
                val searchResults = lrclibApi.searchLyrics(query = "${track.artistName} ${track.title}")
                val matched = searchResults.firstOrNull { !it.syncedLyrics.isNullOrBlank() }
                if (matched?.syncedLyrics != null) {
                    val parsed = LrcParser.parse(matched.syncedLyrics, track.id)
                    if (parsed.lines.isNotEmpty()) return@withContext parsed
                }
            } catch (_: Exception) {}
        }

        CuratedCatalog.getSampleLyrics(track)
    }

    suspend fun toggleLiked(track: Track): Boolean = withContext(Dispatchers.IO) {
        val newLiked = !track.isLiked
        val existing = dao.getTrackById(track.id)
        if (existing == null) {
            dao.upsertTrack(TrackEntity.fromTrack(track.copy(isLiked = newLiked)))
        } else {
            dao.updateLiked(track.id, newLiked)
        }
        newLiked
    }

    suspend fun recordPlay(track: Track) = withContext(Dispatchers.IO) {
        val existing = dao.getTrackById(track.id)
        if (existing == null) {
            dao.upsertTrack(TrackEntity.fromTrack(track.copy(playCount = 1, lastPlayedTimestamp = System.currentTimeMillis())))
        } else {
            dao.recordPlay(track.id, System.currentTimeMillis())
        }
    }

    suspend fun getSmartMix(): List<Track> = withContext(Dispatchers.IO) {
        val topHistory = dao.getTopPlayedTracks(10).firstOrNull() ?: emptyList()
        val allTracks = (topHistory.map { it.toTrack() } + YouTubeMusicCatalog.allTracks + CuratedCatalog.tracks).distinctBy { it.id }
        allTracks.shuffled()
    }

    suspend fun createPlaylist(name: String, description: String = ""): Long = withContext(Dispatchers.IO) {
        dao.insertPlaylist(PlaylistEntity(name = name, description = description))
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        dao.deletePlaylist(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, track: Track) = withContext(Dispatchers.IO) {
        dao.upsertTrack(TrackEntity.fromTrack(track))
        dao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId = playlistId, trackId = track.id))
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) = withContext(Dispatchers.IO) {
        dao.removeTrackFromPlaylist(playlistId, trackId)
    }

    fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>> {
        return dao.getTracksForPlaylist(playlistId).map { list -> list.map { it.toTrack() } }
    }

    suspend fun downloadTrack(track: Track) = downloader.downloadTrack(track)

    suspend fun removeDownload(track: Track) = downloader.removeDownload(track)

    fun getDownloadStatus() = downloader.downloadStatus

    private suspend fun syncWithDatabase(tracks: List<Track>): List<Track> {
        return tracks.map { track ->
            val entity = dao.getTrackById(track.id)
            if (entity != null) {
                track.copy(
                    isLiked = entity.isLiked,
                    localUri = entity.localUri,
                    downloadedAt = entity.downloadedAt,
                    playCount = entity.playCount
                )
            } else {
                track
            }
        }
    }
}
