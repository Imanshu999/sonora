package com.example.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class JamendoResponse(
    val results: List<JamendoTrackDto> = emptyList()
)

@Serializable
data class JamendoTrackDto(
    val id: String,
    val name: String,
    val duration: Int? = null,
    @SerialName("artist_name") val artistName: String,
    @SerialName("album_name") val albumName: String? = null,
    val audio: String,
    val image: String? = null,
    @SerialName("license_ccurl") val licenseCcUrl: String? = null,
    val shareurl: String? = null
)

interface JamendoApi {
    @GET("v3.0/tracks/")
    suspend fun getTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0,
        @Query("boost") boost: String = "popularity_month",
        @Query("include") include: String = "musicinfo",
        @Query("audioformat") audioFormat: String = "mp32"
    ): JamendoResponse

    @GET("v3.0/tracks/")
    suspend fun searchTracks(
        @Query("namesearch") query: String,
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0,
        @Query("audioformat") audioFormat: String = "mp32"
    ): JamendoResponse

    @GET("v3.0/tracks/")
    suspend fun getTracksByTag(
        @Query("tags") tag: String,
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0,
        @Query("audioformat") audioFormat: String = "mp32"
    ): JamendoResponse
}
