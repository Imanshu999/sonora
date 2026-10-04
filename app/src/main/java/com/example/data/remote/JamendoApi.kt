package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class JamendoResponse(
    @Json(name = "results") val results: List<JamendoTrackDto>?
)

@JsonClass(generateAdapter = true)
data class JamendoTrackDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "duration") val duration: Int?,
    @Json(name = "artist_name") val artist_name: String,
    @Json(name = "album_name") val album_name: String?,
    @Json(name = "audio") val audio: String,
    @Json(name = "audiodl") val audiodl: String?,
    @Json(name = "image") val image: String?,
    @Json(name = "audiodlallowed") val audiodlallowed: Boolean?,
    @Json(name = "license_ccurl") val license_ccurl: String?,
    @Json(name = "shareurl") val shareurl: String?
)

interface JamendoApi {
    @GET("v3.0/tracks/")
    suspend fun getTracks(
        @Query("client_id") clientId: String = "c4bfa6c8",
        @Query("format") format: String = "jsonpretty",
        @Query("limit") limit: Int = 30,
        @Query("boost") boost: String = "popularity_month",
        @Query("include") include: String = "musicinfo",
        @Query("audioformat") audioformat: String = "mp32"
    ): JamendoResponse

    @GET("v3.0/tracks/")
    suspend fun searchTracks(
        @Query("namesearch") query: String,
        @Query("client_id") clientId: String = "c4bfa6c8",
        @Query("format") format: String = "jsonpretty",
        @Query("limit") limit: Int = 30,
        @Query("audioformat") audioformat: String = "mp32"
    ): JamendoResponse

    @GET("v3.0/tracks/")
    suspend fun getTracksByTag(
        @Query("tags") tag: String,
        @Query("client_id") clientId: String = "c4bfa6c8",
        @Query("format") format: String = "jsonpretty",
        @Query("limit") limit: Int = 30,
        @Query("audioformat") audioformat: String = "mp32"
    ): JamendoResponse
}
