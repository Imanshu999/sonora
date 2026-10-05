package com.example.data.repository

import com.example.data.local.HomeRowTrackEntity
import com.example.data.local.OfflineDownloader
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistTrackCrossRef
import com.example.data.local.SonoraDao
import com.example.data.local.TrackEntity
import com.example.data.remote.AudiusApi
import com.example.data.remote.JamendoApi
import com.example.data.remote.LrcParser
import com.example.data.remote.LrclibApi
import com.example.data.source.AudiusSource
import com.example.data.source.JamendoSource
import com.example.data.source.FreeToUseSource
import com.example.data.source.MusicSource
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
    private val jamendoClientId: String,
    private val freeToUseApi: com.example.data.remote.FreeToUseApi
) {

    val youTubeSource = YouTubeMusicSource()
    val audiusSource = AudiusSource(audiusApi)
    val jamendoSource = JamendoSource(jamendoApi, jamendoClientId)
    val freeToUseSource = FreeToUseSource(freeToUseApi)

    val sources: List<MusicSource> = listOf(
        youTubeSource,
        audiusSource,
        jamendoSource,
        freeToUseSource
    )

    val likedTracks: Flow<List<Track>> =
        dao.getLikedTracks().map { list -> list.map { it.toTrack() } }

    val downloadedTracks: Flow<List<Track>> =
        dao.getDownloadedTracks().map { list -> list.map { it.toTrack() } }

    val listeningHistory: Flow<List<Track>> =
        dao.getListeningHistory(30).map { list -> list.map { it.toTrack() } }

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
                return@withContext filterRows(
                    cachedRows,
                    language,
                    enabledSources
                )
            }
        }

        val freshRows = coroutineScope {
            HomeRowCatalog.rows.map { config ->
                async {
                    val tracks = mutableListOf<Track>()

                    if ("YOUTUBE" in enabledSources) {
                        tracks += if (config.useTrending) {
                            youTubeSource.getTrending(100)
                        } else {
                            config.queries.flatMap { youTubeSource.search(it, 100) } +
                                listOfNotNull(config.query).flatMap { youTubeSource.search(it, 100) }
                        }
                    }

                    val baseQueries =
                        if (config.queries.isNotEmpty()) {
                            config.queries
                        } else {
                            listOfNotNull(config.query)
                        }

                    val queries =
                        if (language == MusicLanguage.ALL) {
                            baseQueries
                        } else {
                            baseQueries.map { "$it ${language.title}" }
                        }

                    if ("AUDIUS" in enabledSources) {
                        tracks += if (config.useTrending) {
                            audiusSource.getTrending(100)
                        } else {
                            queries.flatMap {
                                audiusSource.search(it, 100)
                            }
                        }
                    }

                    if ("JAMENDO" in enabledSources) {
                        tracks += if (config.useTrending) {
                            jamendoSource.getTrending(100)
                        } else {
                            queries.flatMap {
                                jamendoSource.search(it, 100)
                            }
                        }
                    }

                    if ("FREE_TO_USE" in enabledSources) {
                        tracks += if (config.useTrending) {
                            freeToUseSource.getTrending(100)
                        } else {
                            queries.flatMap {
                                freeToUseSource.search(it, 100)
                            }
                        }
                    }

                    HomeRow(
                        id = config.id,
                        title = config.title,
                        subtitle = config.subtitle,
                        tracks = tracks.distinctBy { it.id }.shuffled().take(60),
                        languageRegion = config.languageRegion
                    )
                }
            }.map { it.await() }
        }

        val syncedRows = freshRows.map { row ->
            row.copy(
                tracks = syncWithDatabase(row.tracks)
            )
        }.filter {
            it.tracks.isNotEmpty()
        }

        try {
            dao.clearHomeRows()

            val tracks = syncedRows
                .flatMap { it.tracks }
                .distinctBy { it.id }

            dao.upsertTracks(
                tracks.map(TrackEntity::fromTrack)
            )

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

        filterRows(
            syncedRows,
            language,
            enabledSources
        )
    }

    private fun parseSourceFilter(sourceFilter: String): Set<String> {
        if (sourceFilter.isBlank() || sourceFilter == "ALL") {
            return setOf(
                "YOUTUBE",
                "AUDIUS",
                "JAMENDO",
                "FREE_TO_USE"
            )
        }

        return sourceFilter
            .split(',')
            .map { it.trim().uppercase() }
            .filter { it.isNotBlank() }
            .toSet()
    }

    private suspend fun buildRowsFromCache(
        entities: List<HomeRowTrackEntity>
    ): List<HomeRow> {
        val rowOrder = HomeRowCatalog.rows
            .mapIndexed { index, config ->
                config.id to index
            }
            .toMap()

        return entities
            .groupBy { it.rowId }
            .toList()
            .sortedBy {
                rowOrder[it.first] ?: Int.MAX_VALUE
            }
            .mapNotNull { (rowId, rowEntities) ->
                val first = rowEntities.minByOrNull {
                    it.sortOrder
                } ?: return@mapNotNull null

                val tracks = rowEntities
                    .sortedBy { it.sortOrder }
                    .mapNotNull {
                        dao.getTrackById(it.trackId)?.toTrack()
                    }

                if (tracks.isEmpty()) {
                    null
                } else {
                    HomeRow(
                        rowId,
                        first.rowTitle,
                        first.rowSubtitle,
                        tracks,
                        first.languageRegion
                    )
                }
            }
    }

    private fun filterRows(
        rows: List<HomeRow>,
        language: MusicLanguage,
        enabledSources: Set<String>
    ): List<HomeRow> {
        val languageFiltered =
            if (language == MusicLanguage.ALL) {
                rows
            } else {
                rows.map { row ->
                    val query = language.title
                        .removeSuffix(" Language")
                        .removeSuffix(" & Bollywood")
                        .lowercase()

                    row.copy(
                        tracks = row.tracks.filter { track ->
                            track.title.contains(query, true) ||
                            track.artistName.contains(query, true) ||
                            track.albumName.contains(query, true) ||
                            track.genre.contains(query, true)
                        }
                    )
                }
            }

        return languageFiltered
            .map { row ->
                row.copy(
                    tracks = row.tracks.filter { track ->
                        sourceIdFor(track.source) in enabledSources
                    }
                )
            }
            .filter {
                it.tracks.isNotEmpty()
            }
    }

    private fun sourceIdFor(source: String): String =
        when (source.lowercase()) {
            "youtube music", "youtube" -> "YOUTUBE"
            "audius" -> "AUDIUS"
            "jamendo", "jamendo (cc)" -> "JAMENDO"
            "freetouse", "free to use", "free_to_use" -> "FREE_TO_USE"
            else -> source.uppercase()
        }

    suspend fun getTrendingTracks(): List<Track> =
        withContext(Dispatchers.IO) {
            val tracks = mutableListOf<Track>()

            tracks += audiusSource.getTrending(20)
            tracks += jamendoSource.getTrending(20)
            tracks += freeToUseSource.getTrending(20)

            if ("YOUTUBE" in parseSourceFilter("ALL")) {
                tracks += youTubeSource.getTrending(40)
            }

            syncWithDatabase(
                tracks.distinctBy { it.id }
            )
        }

    suspend fun getNewReleases(): List<Track> =
        withContext(Dispatchers.IO) {
            val tracks =
                audiusSource.search("new music", 60) +
                jamendoSource.search("new music", 60) +
                freeToUseSource.search("new music", 60)

            syncWithDatabase(
                tracks.distinctBy { it.id }
            )
        }

    suspend fun getTracksByGenre(
        genre: String
    ): List<Track> =
        withContext(Dispatchers.IO) {
            val tracks =
                audiusSource.search(genre, 60) +
                jamendoSource.search(genre, 60) +
                youTubeSource.search(genre, 60) +
                freeToUseSource.search(genre, 60)

            syncWithDatabase(
                tracks.distinctBy { it.id }
            )
        }

    suspend fun search(
        query: String,
        sourceFilter: String = "ALL"
    ): List<Track> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val enabledSources = parseSourceFilter(sourceFilter)
        val normalized = query.trim()

        val results = coroutineScope {
            val jobs = mutableListOf<kotlinx.coroutines.Deferred<List<Track>>>()

            if ("YOUTUBE" in enabledSources) jobs += async { youTubeSource.search(normalized, 500) }
            if ("AUDIUS" in enabledSources) jobs += async { audiusSource.search(normalized, 500) }
            if ("JAMENDO" in enabledSources) jobs += async { jamendoSource.search(normalized, 500) }
            if ("FREE_TO_USE" in enabledSources) jobs += async { freeToUseSource.search(normalized, 500) }

            jobs.flatMap { job -> runCatching { job.await() }.getOrDefault(emptyList()) }
        }

        val q = normalized.lowercase()
        val ranked = results
            .distinctBy { it.id }
            .sortedWith(
                compareByDescending<Track> {
                    when {
                        it.title.equals(normalized, ignoreCase = true) -> 4
                        it.title.contains(q, ignoreCase = true) -> 3
                        it.artistName.contains(q, ignoreCase = true) -> 2
                        it.albumName.contains(q, ignoreCase = true) || it.genre.contains(q, ignoreCase = true) -> 1
                        else -> 0
                    }
                }.thenBy { it.title.lowercase() }
            )

        syncWithDatabase(ranked)
    }

    suspend fun getSyncedLyrics(
        track: Track
    ): SyncedLyrics = withContext(Dispatchers.IO) {
        try {
            val cleanTitle = track.title
                .replace(
                    "\\(.*?\\)|\\[.*?\\]".toRegex(),
                    ""
                )
                .trim()

            val dto = lrclibApi.getLyrics(
                trackName = cleanTitle,
                artistName = track.artistName,
                durationSec = track.durationSeconds
            )

            if (!dto.syncedLyrics.isNullOrBlank()) {
                val parsed = LrcParser.parse(
                    dto.syncedLyrics,
                    track.id
                )

                if (parsed.lines.isNotEmpty()) {
                    return@withContext parsed
                }
            }
        } catch (_: Exception) {
            try {
                val searchResults =
                    lrclibApi.searchLyrics(
                        query = "${track.artistName} ${track.title}"
                    )

                val matched = searchResults.firstOrNull {
                    !it.syncedLyrics.isNullOrBlank()
                }

                if (matched?.syncedLyrics != null) {
                    val parsed = LrcParser.parse(
                        matched.syncedLyrics,
                        track.id
                    )

                    if (parsed.lines.isNotEmpty()) {
                        return@withContext parsed
                    }
                }
            } catch (_: Exception) {
            }
        }

        return@withContext SyncedLyrics(
            trackId = track.id,
            isSynced = false,
            lines = emptyList(),
            rawPlain = ""
        )
    }

    suspend fun toggleLiked(
        track: Track
    ): Boolean = withContext(Dispatchers.IO) {
        val newLiked = !track.isLiked
        val existing = dao.getTrackById(track.id)

        if (existing == null) {
            dao.upsertTrack(
                TrackEntity.fromTrack(
                    track.copy(
                        isLiked = newLiked
                    )
                )
            )
        } else {
            dao.updateLiked(
                track.id,
                newLiked
            )
        }

        newLiked
    }

    suspend fun recordPlay(
        track: Track
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getTrackById(track.id)

        if (existing == null) {
            dao.upsertTrack(
                TrackEntity.fromTrack(
                    track.copy(
                        playCount = 1,
                        lastPlayedTimestamp =
                            System.currentTimeMillis()
                    )
                )
            )
        } else {
            dao.recordPlay(
                track.id,
                System.currentTimeMillis()
            )
        }
    }

    suspend fun getSmartMix(): List<Track> =
        withContext(Dispatchers.IO) {
            val topHistory =
                dao.getTopPlayedTracks(10)
                    .firstOrNull()
                    ?: emptyList()

            topHistory
                .map { it.toTrack() }
                .distinctBy { it.id }
                .shuffled()
        }

    suspend fun createPlaylist(
        name: String,
        description: String = ""
    ): Long = withContext(Dispatchers.IO) {
        dao.insertPlaylist(
            PlaylistEntity(
                name = name,
                description = description
            )
        )
    }

    suspend fun deletePlaylist(
        playlistId: Long
    ) = withContext(Dispatchers.IO) {
        dao.deletePlaylist(playlistId)
    }

    suspend fun addTrackToPlaylist(
        playlistId: Long,
        track: Track
    ) = withContext(Dispatchers.IO) {
        dao.upsertTrack(
            TrackEntity.fromTrack(track)
        )

        dao.addTrackToPlaylist(
            PlaylistTrackCrossRef(
                playlistId = playlistId,
                trackId = track.id
            )
        )
    }

    suspend fun removeTrackFromPlaylist(
        playlistId: Long,
        trackId: String
    ) = withContext(Dispatchers.IO) {
        dao.removeTrackFromPlaylist(
            playlistId,
            trackId
        )
    }

    fun getTracksForPlaylist(
        playlistId: Long
    ): Flow<List<Track>> {
        return dao.getTracksForPlaylist(
            playlistId
        ).map { list ->
            list.map { it.toTrack() }
        }
    }

    suspend fun downloadTrack(
        track: Track
    ) = downloader.downloadTrack(track)

    suspend fun removeDownload(
        track: Track
    ) = downloader.removeDownload(track)

    fun getDownloadStatus() =
        downloader.downloadStatus

    private suspend fun syncWithDatabase(
        tracks: List<Track>
    ): List<Track> {
        return tracks.map { trackItem ->
            val entity = dao.getTrackById(trackItem.id)

            if (entity != null) {
                trackItem.copy(
                    isLiked = entity.isLiked,
                    localUri = entity.localUri,
                    downloadedAt = entity.downloadedAt,
                    playCount = entity.playCount
                )
            } else {
                trackItem
            }
        }
    }
}
