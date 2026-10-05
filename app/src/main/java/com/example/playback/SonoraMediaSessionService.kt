package com.example.playback

import android.content.Intent
import android.os.IBinder
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class SonoraMediaSessionService : MediaSessionService() {

    companion object {
        @Volatile
        var activePlayer: Player? = null
    }

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        activePlayer?.let { player ->
            mediaSession = MediaSession.Builder(this, player)
                .setId("sonora")
                .build()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (activePlayer?.isPlaying != true) stopSelf()
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = super.onBind(intent)
}
