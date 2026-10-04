package com.example.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class FreeToUseResponse<T>(
    val ok: Boolean = false,
    val data: T? = null,
    val pagination: FreeToUsePagination? = null,
    val error: String? = null
)

@Serializable
data class FreeToUsePagination(
    val limit: Int? = null,
    val offset: Int = 0,
    val count: Int = 0
)

@Serializable
data class FreeToUseTrackDto(
    val id: String,
    val title: String,
    val artists: List<List<kotlinx.serialization.json.JsonElement>> = emptyList(),
    val genre: String? = null,
    val duration: Double = 0.0,
    val record_label: String? = null,
    val is_premium: Boolean = false,
    val thumbnails: FreeToUseThumbnails? = null,
    val files: FreeToUseFiles? = null,
    val tags: List<List<kotlinx.serialization.json.JsonElement>> = emptyList(),
    val categories: List<List<kotlinx.serialization.json.JsonElement>> = emptyList()
)

@Serializable
data class FreeToUseThumbnails(
    val sm: String? = null,
    val md: String? = null,
    val lg: String? = null,
    val xl: String? = null
)

@Serializable
data class FreeToUseFiles(
    val mp3: String? = null
)

interface FreeToUseApi {
    @GET("music/tracks/all")
    suspend fun getAllTracks(
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0,
        @Query("order") order: String = "release_date",
        @Query("sort") sort: String = "desc"
    ): FreeToUseResponse<List<FreeToUseTrackDto>>

    @GET("music/tracks/search")
    suspend fun searchTracks(
        @Query("query") query: String,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0,
        @Query("order") order: String = "release_date",
        @Query("sort") sort: String = "desc"
    ): FreeToUseResponse<List<FreeToUseTrackDto>>

    @GET("music/tracks/{id}")
    suspend fun getTrack(
        @Path("id") id: String
    ): FreeToUseResponse<FreeToUseTrackDto>
}
