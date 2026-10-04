package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.remote.LrcParser
import com.example.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Sonora", appName)
    }

    @Test
    fun `lrc parser correctly parses timestamps and lyrics`() {
        val lrc = """
            [00:04.20] Walking through the city lights
            [00:12.50] Neon reflections in the rain
            [01:05.123] Dancing through the midnight sky
        """.trimIndent()

        val parsed = LrcParser.parse(lrc, "test_track")
        assertTrue(parsed.isSynced)
        assertEquals(3, parsed.lines.size)
        assertEquals(4200L, parsed.lines[0].timeMs)
        assertEquals("Walking through the city lights", parsed.lines[0].text)
        assertEquals(12500L, parsed.lines[1].timeMs)
        assertEquals(65123L, parsed.lines[2].timeMs)
    }

    @Test
    fun `track formatted duration is accurate`() {
        val track = Track(
            id = "t1",
            title = "Midnight Horizon",
            artistName = "Aura Bloom",
            durationSeconds = 214,
            audioUrl = "https://example.com/audio.mp3",
            artworkUrl = "https://example.com/art.jpg"
        )
        assertEquals("3:34", track.formattedDuration)
    }
}
