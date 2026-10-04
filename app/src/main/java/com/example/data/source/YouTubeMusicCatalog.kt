package com.example.data.source

import com.example.model.Track

object YouTubeMusicCatalog {

    val trendingIndia: List<Track> = listOf(
        Track(
            id = "yt_ind_01",
            title = "Soulmate",
            artistName = "Arijit Singh & Badshah",
            albumName = "Ek Tha Raja",
            durationSeconds = 215,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500",
            source = "YouTube Music",
            genre = "Trending India"
        ),
        Track(
            id = "yt_ind_02",
            title = "O Maahi",
            artistName = "Arijit Singh & Pritam",
            albumName = "Dunki",
            durationSeconds = 233,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500",
            source = "YouTube Music",
            genre = "Trending India"
        ),
        Track(
            id = "yt_ind_03",
            title = "Tauba Tauba",
            artistName = "Karan Aujla",
            albumName = "Bad Newz",
            durationSeconds = 208,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500",
            source = "YouTube Music",
            genre = "Trending India"
        ),
        Track(
            id = "yt_ind_04",
            title = "Husn",
            artistName = "Anuv Jain",
            albumName = "Husn - Single",
            durationSeconds = 218,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=500",
            source = "YouTube Music",
            genre = "Trending India"
        )
    )

    val newHindiReleases: List<Track> = listOf(
        Track(
            id = "yt_hin_01",
            title = "Sajni",
            artistName = "Arijit Singh & Ram Sampath",
            albumName = "Laapataa Ladies",
            durationSeconds = 170,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500",
            source = "YouTube Music",
            genre = "Hindi Releases"
        ),
        Track(
            id = "yt_hin_02",
            title = "Ve Kamleya",
            artistName = "Arijit Singh & Shreya Ghoshal",
            albumName = "Rocky Aur Rani Kii Prem Kahaani",
            durationSeconds = 246,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500",
            source = "YouTube Music",
            genre = "Hindi Releases"
        ),
        Track(
            id = "yt_hin_03",
            title = "Chaleya",
            artistName = "Anirudh Ravichander & Arijit Singh",
            albumName = "Jawan",
            durationSeconds = 200,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=500",
            source = "YouTube Music",
            genre = "Hindi Releases"
        )
    )

    val punjabiHits: List<Track> = listOf(
        Track(
            id = "yt_pun_01",
            title = "Lover",
            artistName = "Diljit Dosanjh",
            albumName = "MoonChild Era",
            durationSeconds = 191,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=500",
            source = "YouTube Music",
            genre = "Punjabi Hits"
        ),
        Track(
            id = "yt_pun_02",
            title = "Brown Munde",
            artistName = "AP Dhillon, Gurinder Gill, Shinda Kahlon",
            albumName = "Brown Munde",
            durationSeconds = 267,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500",
            source = "YouTube Music",
            genre = "Punjabi Hits"
        ),
        Track(
            id = "yt_pun_03",
            title = "Softly",
            artistName = "Karan Aujla & Ikky",
            albumName = "Making Memories",
            durationSeconds = 155,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500",
            source = "YouTube Music",
            genre = "Punjabi Hits"
        ),
        Track(
            id = "yt_pun_04",
            title = "295",
            artistName = "Sidhu Moose Wala",
            albumName = "Moosetape",
            durationSeconds = 270,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500",
            source = "YouTube Music",
            genre = "Punjabi Hits"
        )
    )

    val bollywoodClassics: List<Track> = listOf(
        Track(
            id = "yt_cls_01",
            title = "Tum Hi Ho",
            artistName = "Arijit Singh & Mithoon",
            albumName = "Aashiqui 2",
            durationSeconds = 262,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500",
            source = "YouTube Music",
            genre = "Bollywood Classics"
        ),
        Track(
            id = "yt_cls_02",
            title = "Kun Faya Kun",
            artistName = "A.R. Rahman, Javed Ali, Mohit Chauhan",
            albumName = "Rockstar",
            durationSeconds = 473,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500",
            source = "YouTube Music",
            genre = "Bollywood Classics"
        ),
        Track(
            id = "yt_cls_03",
            title = "Kal Ho Naa Ho",
            artistName = "Sonu Nigam & Shankar-Ehsaan-Loy",
            albumName = "Kal Ho Naa Ho",
            durationSeconds = 321,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=500",
            source = "YouTube Music",
            genre = "Bollywood Classics"
        )
    )

    val romanticTracks: List<Track> = listOf(
        Track(
            id = "yt_rom_01",
            title = "Kesariya",
            artistName = "Arijit Singh & Pritam",
            albumName = "Brahmastra",
            durationSeconds = 268,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500",
            source = "YouTube Music",
            genre = "Romantic"
        ),
        Track(
            id = "yt_rom_02",
            title = "Raataan Lambiyan",
            artistName = "Jubin Nautiyal & Asees Kaur",
            albumName = "Shershaah",
            durationSeconds = 230,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=500",
            source = "YouTube Music",
            genre = "Romantic"
        )
    )

    val workoutTracks: List<Track> = listOf(
        Track(
            id = "yt_wrk_01",
            title = "Zinda",
            artistName = "Siddharth Mahadevan",
            albumName = "Bhaag Milkha Bhaag",
            durationSeconds = 211,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500",
            source = "YouTube Music",
            genre = "Workout"
        ),
        Track(
            id = "yt_wrk_02",
            title = "Kar Har Maidaan Fateh",
            artistName = "Sukhwinder Singh & Shreya Ghoshal",
            albumName = "Sanju",
            durationSeconds = 311,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500",
            source = "YouTube Music",
            genre = "Workout"
        )
    )

    val lofiTracks: List<Track> = listOf(
        Track(
            id = "yt_lofi_01",
            title = "Iktara (Lo-fi Flip)",
            artistName = "Midnight Chai Beats",
            albumName = "Bombay Rains",
            durationSeconds = 175,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500",
            source = "YouTube Music",
            genre = "Lo-fi"
        ),
        Track(
            id = "yt_lofi_02",
            title = "Agar Tum Saath Ho (Chillhop Edit)",
            artistName = "Studio Delhi",
            albumName = "Nostalgia Vibes",
            durationSeconds = 192,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=500",
            source = "YouTube Music",
            genre = "Lo-fi"
        )
    )

    val devotionalTracks: List<Track> = listOf(
        Track(
            id = "yt_dev_01",
            title = "Hanuman Chalisa",
            artistName = "Hariharan & Gulshan Kumar",
            albumName = "Shree Hanuman Chalisa",
            durationSeconds = 582,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500",
            source = "YouTube Music",
            genre = "Devotional"
        ),
        Track(
            id = "yt_dev_02",
            title = "Achyutam Keshavam",
            artistName = "Shreya Ghoshal",
            albumName = "Sacred Chants",
            durationSeconds = 315,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            artworkUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=500",
            source = "YouTube Music",
            genre = "Devotional"
        )
    )

    val allTracks: List<Track> = (trendingIndia + newHindiReleases + punjabiHits + bollywoodClassics + romanticTracks + workoutTracks + lofiTracks + devotionalTracks).distinctBy { it.id }
}
