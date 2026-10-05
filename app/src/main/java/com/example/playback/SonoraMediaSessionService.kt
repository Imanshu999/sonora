package com.example.playback

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.SonoraApplication

/**
 * Owns the MediaSession used by Android's system media controls/lock screen.
 * The actual ExoPlayer remains owned by SonoraApplication so the Compose UI and
 * the service always operate on the same playback queue.
 */
class SonoraMediaSessionService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val player = SonoraApplication.instance.player.exoPlayer
        val sessionActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            1001,
            sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setId("SonoraMediaSession")
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onUpdateNotification(session: MediaSession, startInForeground: Boolean) {
        // Media3 owns the actual notification. Explicitly promote it while audio is active.
        super.onUpdateNotification(session, startInForeground)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Keep background audio alive when the user swipes Sonora away from Recents.
        // Media3 will stop the service normally once playback is no longer ongoing.
        if (mediaSession?.player?.isPlaying == true) return
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        mediaSession?.runCatching { release() }
        mediaSession = null
        super.onDestroy()
    }
}
