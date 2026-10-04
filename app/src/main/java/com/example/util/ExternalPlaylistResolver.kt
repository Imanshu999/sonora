package com.example.util

import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI

/**
 * Resolves external playlist links without scraping. The archive supplied for
 * this build does not contain the claimed InnerTube/NewPipeExtractor layer, so
 * URL resolution is deliberately isolated here rather than faking track data.
 *
 * A provider can be plugged in later when its official API/authentication is
 * configured. CSV imports remain fully offline and require no credentials.
 */
interface ExternalPlaylistProvider {
    fun canHandle(uri: URI): Boolean
    suspend fun resolve(uri: URI): ExternalPlaylist
}

data class ExternalPlaylist(
    val name: String,
    val tracks: List<ParsedTrackItem>
)

class ExternalPlaylistResolver(
    private val providers: List<ExternalPlaylistProvider> = emptyList()
) {
    suspend fun resolve(input: String): ExternalPlaylist = withContext(Dispatchers.IO) {
        val uri = runCatching { URI(input.trim()) }.getOrElse {
            throw IllegalArgumentException("Invalid playlist URL")
        }
        val provider = providers.firstOrNull { it.canHandle(uri) }
            ?: throw IllegalArgumentException(
                "This build has no official API provider configured for that playlist URL. " +
                    "CSV import works offline; URL imports require the provider's official API credentials."
            )
        provider.resolve(uri)
    }
}
