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
import com.example.model.HomeRowCatalog
import com.example.model.MusicLanguage
import com.example.model.Playlist
import com.example.model.SyncedLyrics
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MusicRepository(
    private val jamendoApi: JamendoApi,
    private val audiusApi: AudiusApi,
    private val lrclibApi: LrclibApi,
    private val dao: SonoraDao,
    private val downloader: OfflineDownloader,
    private val jamendoClientId: String
) {

    val youTubeSource = YouTubeMusicSource()
    val audiusSource = AudiusSource(audiusApi)
    val jamendoSource = JamendoSource(jamendoApi, jamendoClientId)

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
        val enabledSources = parseSourceFilter(sourceFilter)

        if (!forceRefresh) {
            val cachedEntities = dao.getAllCachedHomeRows().firstOrNull().orEmpty()
            val cachedRows = buildRowsFromCache(cachedEntities)
            if (cachedRows.isNotEmpty()) {
                return@withContext filterRows(cachedRows, language, enabledSources)
            }
        }

        val freshRows = coroutineScope {
            HomeRowCatalog.rows.map { config ->
                async {
                    val tracks = mutableListOf<Track>()

                    if ("YOUTUBE" in enabledSources) {
                        tracks += youtubeCatalogTracks(config.catalogKey, config.query)
                    }

                    val queries = if (config.queries.isNotEmpty()) config.queries else listOfNotNull(config.query)

                    if ("AUDIUS" in enabledSources) {
                        tracks += if (config.id == "trending_india") {
                            audiusSource.getTrending(10)
                        } else {
                            queries.flatMap { audiusSource.search(it, 4) }
                        }
                    }

                    if ("JAMENDO" in enabledSources) {
                        tracks += if (config.id == "trending_india") {
                            jamendoSource.getTrending(10)
                        } else {
                            queries.flatMap { jamendoSource.search(it, 4) }
                        }
                    }

                    HomeRow(
                        id = config.id,
                        title = config.title,
                        subtitle = config.subtitle,
                        tracks = tracks.distinctBy { it.id }.take(12),
                        languageRegion = config.languageRegion
                    )
                }
            }.map { it.await() }
        }

        val syncedRows = freshRows.map { row ->
            row.copy(tracks = syncWithDatabase(row.tracks))
        }.filter { it.tracks.isNotEmpty() }

        try {
            dao.clearHomeRows()
            val tracks = syncedRows.flatMap { it.tracks }.distinctBy { it.id }
            dao.upsertTracks(tracks.map(TrackEntity::fromTrack))
            dao.insertHomeRowTracks(
                syncedRows.flatMap { row ->
                    row.tracks.mapIndexed { index, track ->
                        HomeRowTrackEntity(
                            rowId = row.id,
                            trackId = track.id,
                            rowTitle = row.title,
                            rowSubtitle = row.subtitle,
                            languageRegion = row.languageRegion,
                            sortOrder = index
                        )
                    }
                }
            )
        } catch (_: Exception) {
            // A network response should still be usable if the local cache is unavailable.
        }

        filterRows(syncedRows, language, enabledSources)
    }

    private fun parseSourceFilter(sourceFilter: String): Set<String> {
        if (sourceFilter.isBlank() || sourceFilter == "ALL") {
            return setOf("YOUTUBE", "AUDIUS", "JAMENDO")
        }
        return sourceFilter.split(',').map { it.trim().uppercase() }.filter { it.isNotBlank() }.toSet()
    }

    private fun youtubeCatalogTracks(catalogKey: String?, query: String?): List<Track> {
        return when (catalogKey) {
            "trendingIndia" -> YouTubeMusicCatalog.trendingIndia
            "newHindiReleases" -> YouTubeMusicCatalog.newHindiReleases
            "punjabiHits" -> YouTubeMusicCatalog.punjabiHits
            "bollywoodClassics" -> YouTubeMusicCatalog.bollywoodClassics
            "romanticTracks" -> YouTubeMusicCatalog.romanticTracks
            "workoutTracks" -> YouTubeMusicCatalog.workoutTracks
            "lofiTracks" -> YouTubeMusicCatalog.lofiTracks
            "devotionalTracks" -> YouTubeMusicCatalog.devotionalTracks
            else -> query?.let { q ->
                YouTubeMusicCatalog.allTracks.filter {
                    it.title.contains(q, true) || it.artistName.contains(q, true) || it.genre.contains(q, true)
                }.take(12)
            }.orEmpty()
        }
    }

    private fun buildRowsFromCache(entities: List<HomeRowTrackEntity>): List<HomeRow> {
        val rowOrder = HomeRowCatalog.rows.mapIndexed { index, config -> config.id to index }.toMap()
        return entities.groupBy { it.rowId }.toList().sortedBy { rowOrder[it.first] ?: Int.MAX_VALUE }.mapNotNull { (rowId, rowEntities) ->
            val first = rowEntities.minByOrNull { it.sortOrder } ?: return@mapNotNull null
            val tracks = rowEntities.sortedBy { it.sortOrder }.mapNotNull { dao.getTrackById(it.trackId)?.toTrack() }
            if (tracks.isEmpty()) null else HomeRow(rowId, first.rowTitle, first.rowSubtitle, tracks, first.languageRegion)
        }
    }

    private fun filterRows(
        rows: List<HomeRow>,
        language: MusicLanguage,
        enabledSources: Set<String>
    ): List<HomeRow> {
        val languageFiltered = when (language) {
            MusicLanguage.ALL -> rows
            MusicLanguage.HINDI -> rows.filter { it.languageRegion == "ALL" || it.languageRegion == "HINDI" }
            MusicLanguage.PUNJABI -> rows.filter { it.languageRegion == "ALL" || it.languageRegion == "PUNJABI" }
            MusicLanguage.ENGLISH -> rows.filter { it.languageRegion == "ALL" || it.languageRegion == "ENGLISH" }
        }
        return languageFiltered.map { row ->
            row.copy(tracks = row.tracks.filter { track -> sourceIdFor(track.source) in enabledSources })
        }.filter { it.tracks.isNotEmpty() }
    }

    private fun sourceIdFor(source: String): String = when (source.lowercase()) {
        "youtube music", "youtube" -> "YOUTUBE"
        "audius" -> "AUDIUS"
        "jamendo", "jamendo (cc)" -> "JAMENDO"
        else -> source.uppercase()
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

        val enabledSources = parseSourceFilter(sourceFilter)

        // 1. YouTube Music search
        if ("YOUTUBE" in enabledSources) {
            try {
                results.addAll(youTubeSource.search(query))
            } catch (_: Exception) {}
        }

        // 2. Audius search
        if ("AUDIUS" in enabledSources) {
            try {
                results.addAll(audiusSource.search(query))
            } catch (_: Exception) {}
        }

        // 3. Jamendo search
        if ("JAMENDO" in enabledSources) {
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
