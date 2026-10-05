package com.example.model

enum class AudioQuality(val title: String, val subtitle: String) {
    HIGH("High Quality (320 kbps)", "Crystal clear audio, best for Wi-Fi"),
    STANDARD("Standard (192 kbps)", "Balanced sound and data efficiency"),
    DATA_SAVER("Data Saver (96 kbps)", "Conserves bandwidth on mobile networks")
}

enum class SonoraTheme(val title: String) {
    LIGHT("Light / White"),
    SYSTEM("Follow System"),
    DARK("Deep Midnight"),
    AMOLED("Pure Black (AMOLED)")
}

enum class EqualizerPreset(val title: String, val gains: List<Int>) {
    FLAT("Flat", listOf(0, 0, 0, 0, 0)),
    BASS_BOOST("Bass Boost", listOf(6, 4, 1, 0, 0)),
    VOCAL_BOOST("Vocal", listOf(-1, 2, 5, 3, 0)),
    ELECTRONIC("Electronic", listOf(5, 3, 0, 2, 4)),
    ACOUSTIC("Acoustic", listOf(3, 2, 2, 3, 2)),
    ROCK("Rock", listOf(5, 2, -1, 3, 5))
}

enum class RepeatMode {
    OFF, ALL, ONE
}
