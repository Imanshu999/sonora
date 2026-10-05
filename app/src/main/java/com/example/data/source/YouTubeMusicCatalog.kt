package com.example.data.source

import com.example.model.Track

/**
 * No fabricated commercial catalog is bundled.
 *
 * YouTube can be used for metadata/discovery only when a properly configured
 * and policy-compliant API integration is supplied. A YouTube watch URL is not
 * a direct audio stream and must not be fed to ExoPlayer as if it were one.
 */
object YouTubeMusicCatalog {
    val allTracks: List<Track> = emptyList()
}
