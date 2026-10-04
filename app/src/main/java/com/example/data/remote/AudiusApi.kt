package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class AudiusTracksResponse(
    @Json(name = "data") val data: List<AudiusTrackDto>?
)

@JsonClass(generateAdapter = true)
data class AudiusTrackDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "duration") val duration: Int?,
    @Json(name = "user") val user: AudiusUserDto?,
    @Json(name = "artwork") val artwork: AudiusArtworkDto?,
    @Json(name = "genre") val genre: String?
)

@JsonClass(generateAdapter = true)
data class AudiusUserDto(
    @Json(name = "name") val name: String?
)

@JsonClass(generateAdapter = true)
data class AudiusArtworkDto(
    @Json(name = "150x150") val small: String?,
    @Json(name = "480x480") val medium: String?,
    @Json(name = "1000x1000") val large: String?
)

interface AudiusApi {
    @GET("v1/tracks/trending")
    suspend fun getTrending(
        @Query("app_name") appName: String = "SONORA_STREAM"
    ): AudiusTracksResponse

    @GET("v1/tracks/search")
    suspend fun searchTracks(
        @Query("query") query: String,
        @Query("app_name") appName: String = "SONORA_STREAM"
    ): AudiusTracksResponse
}
