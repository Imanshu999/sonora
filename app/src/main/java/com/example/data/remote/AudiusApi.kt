package com.example.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class AudiusTracksResponse(
    val data: List<AudiusTrackDto> = emptyList()
)

@Serializable
data class AudiusTrackDto(
    val id: String,
    val title: String,
    val duration: Int? = null,
    val user: AudiusUserDto? = null,
    val artwork: AudiusArtworkDto? = null,
    val genre: String? = null
)

@Serializable
data class AudiusUserDto(val name: String? = null)

@Serializable
data class AudiusArtworkDto(
    @SerialName("150x150") val small: String? = null,
    @SerialName("480x480") val medium: String? = null,
    @SerialName("1000x1000") val large: String? = null
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
