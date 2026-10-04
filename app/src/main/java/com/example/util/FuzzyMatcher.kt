package com.example.util

import com.example.model.Track
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

object FuzzyMatcher {

    fun cleanString(text: String): String {
        return text.lowercase(Locale.ROOT)
            .replace("\\(.*?\\)|\\[.*?\\]".toRegex(), "") // remove parentheses content
            .replace("official\\s*(video|audio|music\\s*video|lyric\\s*video|visualizer)".toRegex(), "")
            .replace("feat\\..*?|ft\\..*?".toRegex(), "")
            .replace("[^a-zA-Z0-9\\s]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    fun similarity(s1: String, s2: String): Float {
        val c1 = cleanString(s1)
        val c2 = cleanString(s2)
        if (c1.isEmpty() && c2.isEmpty()) return 1f
        if (c1.isEmpty() || c2.isEmpty()) return 0f
        if (c1 == c2) return 1f
        if (c1.contains(c2) || c2.contains(c1)) return 0.9f

        val maxLen = max(c1.length, c2.length)
        if (maxLen == 0) return 1f
        val dist = levenshteinDistance(c1, c2)
        return (1f - dist.toFloat() / maxLen.toFloat()).coerceIn(0f, 1f)
    }

    data class MatchScore(
        val track: Track,
        val score: Float
    )

    fun findBestMatch(
        targetTitle: String,
        targetArtist: String,
        candidates: List<Track>,
        threshold: Float = 0.55f
    ): Track? {
        if (candidates.isEmpty()) return null

        var bestScore = 0f
        var bestTrack: Track? = null

        for (candidate in candidates) {
            val titleSim = similarity(targetTitle, candidate.title)
            val artistSim = if (targetArtist.isBlank()) 0.8f else similarity(targetArtist, candidate.artistName)

            // Weighted combination: title 65%, artist 35%
            val totalScore = (titleSim * 0.65f) + (artistSim * 0.35f)

            if (totalScore > bestScore) {
                bestScore = totalScore
                bestTrack = candidate
            }
        }

        return if (bestScore >= threshold) bestTrack else null
    }
}
