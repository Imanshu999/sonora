package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SonoraDao {

    @Query("SELECT * FROM tracks WHERE isLiked = 1 ORDER BY lastPlayedTimestamp DESC")
    fun getLikedTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE localUri IS NOT NULL ORDER BY downloadedAt DESC")
    fun getDownloadedTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE lastPlayedTimestamp IS NOT NULL ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    fun getListeningHistory(limit: Int = 30): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE playCount > 0 ORDER BY playCount DESC LIMIT :limit")
    fun getTopPlayedTracks(limit: Int = 20): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: String): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTrack(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTracks(tracks: List<TrackEntity>)

    @Query("UPDATE tracks SET isLiked = :isLiked WHERE id = :id")
    suspend fun updateLiked(id: String, isLiked: Boolean)

    @Query("UPDATE tracks SET localUri = :localUri, downloadedAt = :downloadedAt WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, localUri: String?, downloadedAt: Long?)

    @Query("UPDATE tracks SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE id = :id")
    suspend fun recordPlay(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossRef)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String)

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_tracks pt ON t.id = pt.trackId
        WHERE pt.playlistId = :playlistId
        ORDER BY pt.addedAt DESC
    """)
    fun getTracksForPlaylist(playlistId: Long): Flow<List<TrackEntity>>

    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getPlaylistTrackCount(playlistId: Long): Int

    @Query("""
        SELECT t.artworkUrl FROM tracks t
        INNER JOIN playlist_tracks pt ON t.id = pt.trackId
        WHERE pt.playlistId = :playlistId
        ORDER BY pt.addedAt DESC LIMIT 1
    """)
    suspend fun getPlaylistCoverUrl(playlistId: Long): String?

    @Query("SELECT * FROM home_rows ORDER BY sortOrder ASC")
    fun getAllCachedHomeRows(): Flow<List<HomeRowTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeRowTracks(tracks: List<HomeRowTrackEntity>)

    @Query("DELETE FROM home_rows")
    suspend fun clearHomeRows()
}
