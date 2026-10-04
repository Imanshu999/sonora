package com.example.data.remote

import com.example.model.LyricLine
import com.example.model.SyncedLyrics
import com.example.model.Track

object CuratedCatalog {

    val tracks: List<Track> = listOf(
        Track(
            id = "audius_07xZblV",
            title = "Traigo Prisa",
            artistName = "Ljazz",
            albumName = "Un Disco Entero",
            durationSeconds = 185,
            audioUrl = "https://discoveryprovider.audius.co/v1/tracks/07xZblV/stream?app_name=SONORA",
            artworkUrl = "https://audius-01.staked.cloud/content/baeaaaiqseapxtgbe4otremldtvtmh75pyp32mvhwkat54sdthmxhyhai3gqwg/480x480.jpg",
            source = "Audius",
            genre = "R&B / Soul",
            licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
            shareUrl = "https://audius.co"
        ),
        Track(
            id = "audius_xkQaGx",
            title = "Night Drive",
            artistName = "Solaris Echo",
            albumName = "Neon Horizon",
            durationSeconds = 210,
            audioUrl = "https://discoveryprovider.audius.co/v1/tracks/xkQaGx/stream?app_name=SONORA",
            artworkUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500",
            source = "Audius",
            genre = "Synthwave",
            licenseUrl = "https://creativecommons.org/licenses/by-sa/4.0/",
            shareUrl = "https://audius.co"
        ),
        Track(
            id = "audius_O5lQz",
            title = "Lofi Sunset",
            artistName = "Chill Collective",
            albumName = "Coffee & Rain",
            durationSeconds = 195,
            audioUrl = "https://discoveryprovider.audius.co/v1/tracks/O5lQz/stream?app_name=SONORA",
            artworkUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500",
            source = "Audius",
            genre = "Lo-Fi",
            licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
            shareUrl = "https://audius.co"
        ),
        Track(
            id = "audius_qbMN0AE",
            title = "Cybernetic Dreams",
            artistName = "Zero Vector",
            albumName = "Matrix Overdrive",
            durationSeconds = 224,
            audioUrl = "https://discoveryprovider.audius.co/v1/tracks/qbMN0AE/stream?app_name=SONORA",
            artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500",
            source = "Audius",
            genre = "Electronic",
            licenseUrl = "https://creativecommons.org/licenses/by-nc/4.0/",
            shareUrl = "https://audius.co"
        ),
        Track(
            id = "audius_k259kWP",
            title = "Acoustic Whispers",
            artistName = "Wilder & Stone",
            albumName = "Valley of Shadows",
            durationSeconds = 188,
            audioUrl = "https://discoveryprovider.audius.co/v1/tracks/k259kWP/stream?app_name=SONORA",
            artworkUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=500",
            source = "Audius",
            genre = "Acoustic",
            licenseUrl = "https://creativecommons.org/licenses/by/3.0/",
            shareUrl = "https://audius.co"
        ),
        Track(
            id = "sh_helix_1",
            title = "Electric Symphony",
            artistName = "SoundHelix",
            albumName = "Sonic Dimensions",
            durationSeconds = 372,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500",
            source = "Audius",
            genre = "Electronic",
            licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
            shareUrl = "https://audius.co"
        ),
        Track(
            id = "sh_helix_2",
            title = "Midnight Odyssey",
            artistName = "SoundHelix",
            albumName = "Deep Space",
            durationSeconds = 423,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=500",
            source = "Audius",
            genre = "Ambient",
            licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
            shareUrl = "https://audius.co"
        ),
        Track(
            id = "sh_helix_3",
            title = "Velocity Beat",
            artistName = "SoundHelix",
            albumName = "Club Essentials",
            durationSeconds = 345,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500",
            source = "Audius",
            genre = "Dance",
            licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
            shareUrl = "https://audius.co"
        ),
        Track(
            id = "sh_helix_8",
            title = "Cosmic Velvet",
            artistName = "SoundHelix",
            albumName = "Infinite Orbit",
            durationSeconds = 312,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=500",
            source = "Audius",
            genre = "Chillout",
            licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
            shareUrl = "https://audius.co"
        )
    )

    fun getSampleLyrics(track: Track): SyncedLyrics {
        val lines = listOf(
            LyricLine(0L, "♪ (Intro instrumental) ♪"),
            LyricLine(6000L, "Walking through the city in the quiet of the night"),
            LyricLine(12000L, "Reflections in the rain under purple neon lights"),
            LyricLine(19000L, "Every heartbeat echoes like a rhythm in my soul"),
            LyricLine(26000L, "Taking back the pieces making broken spirits whole"),
            LyricLine(33000L, "♪ (Bass groove building up) ♪"),
            LyricLine(40000L, "Can you feel the frequency rising in the air?"),
            LyricLine(47000L, "Lost inside the melody without a single care"),
            LyricLine(54000L, "We belong to this sonic sky"),
            LyricLine(61000L, "Spread your wings and watch the worries fly"),
            LyricLine(68000L, "♪ (Drop & melodic chorus) ♪"),
            LyricLine(82000L, "Midnight horizon calling our names"),
            LyricLine(89000L, "Dancing in shadows burning like flames"),
            LyricLine(96000L, "Nothing can stop this sound tonight"),
            LyricLine(103000L, "Guided by electric starlight"),
            LyricLine(112000L, "♪ (Melodic bridge) ♪"),
            LyricLine(125000L, "Echoes in the wind keep whispering stay"),
            LyricLine(132000L, "Until the morning washes darkness away"),
            LyricLine(139000L, "Can you feel the frequency rising in the air?"),
            LyricLine(146000L, "Lost inside the melody without a single care"),
            LyricLine(154000L, "We belong to this sonic sky"),
            LyricLine(168000L, "♪ (Outro fade out) ♪")
        )
        return SyncedLyrics(
            trackId = track.id,
            isSynced = true,
            lines = lines,
            rawPlain = lines.joinToString("\n") { it.text }
        )
    }
}
