package com.example.playback

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.core.content.ContextCompat
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.model.EqualizerPreset
import com.example.model.RepeatMode
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isShuffleEnabled: Boolean = false,
    val queue: List<Track> = emptyList(),
    val queueIndex: Int = -1,
    val sleepTimerRemainingSec: Int? = null,
    val errorMessage: String? = null
)

class SonoraPlayer(
    private val context: Context,
    private val onTrackFinished: (Track) -> Unit = {}
) {
    private val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 Sonora/1.0")
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(15000)
        .setReadTimeoutMs(20000)

    private val mediaSourceFactory = DefaultMediaSourceFactory(
        DefaultDataSource.Factory(context, httpDataSourceFactory)
    )

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(mediaSourceFactory)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true // handle audio focus automatically
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    val equalizerHelper = EqualizerHelper()

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var loadJob: Job? = null
    @Volatile private var released = false

    private var crossfadeSeconds: Int = 2
    private var originalQueue: List<Track> = emptyList()

    init {
        SonoraMediaSessionService.activePlayer = exoPlayer
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (released) return
                _state.value = _state.value.copy(isPlaying = isPlaying)
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (released) return
                val id = mediaItem?.mediaId ?: return
                val index = _state.value.queue.indexOfFirst { it.id == id }
                if (index >= 0) {
                    val track = _state.value.queue[index]
                    _state.value = _state.value.copy(
                        currentTrack = track,
                        queueIndex = index,
                        currentPositionMs = exoPlayer.currentPosition,
                        durationMs = if (exoPlayer.duration > 0) exoPlayer.duration
                            else (track.durationSeconds * 1000L).coerceAtLeast(1000L),
                        errorMessage = null
                    )
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (released) return
                val isLoading = playbackState == Player.STATE_BUFFERING
                val duration = if (exoPlayer.duration > 0) exoPlayer.duration else 0L
                _state.value = _state.value.copy(
                    isLoading = isLoading,
                    errorMessage = null,
                    durationMs = if (duration > 0) duration else _state.value.durationMs
                )

                if (playbackState == Player.STATE_ENDED) {
                    if (!released) {
                        _state.value.currentTrack?.let { onTrackFinished(it) }
                        handleTrackEnded()
                    }
                }

                if (playbackState == Player.STATE_READY) {
                    equalizerHelper.bindAudioSession(exoPlayer.audioSessionId)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                if (released) return
                Log.e("SonoraPlayer", "Playback error on track: ${error.message}", error)
                _state.value = _state.value.copy(
                    isPlaying = false,
                    isLoading = false,
                    errorMessage = "This stream could not be played. Try another track."
                )
                // Do not automatically replace the current screen/queue after a failed
                // stream. Automatic next() made consecutive bad URLs look like the app
                // was navigating backwards or closing. The user can choose Next manually.
                exoPlayer.pause()
            }
        })
    }

    fun playTrack(track: Track, newQueue: List<Track>? = null) {
        if (released || track.playbackUri.isBlank()) {
            _state.value = _state.value.copy(
                isPlaying = false,
                isLoading = false,
                errorMessage = "This track has no playable stream."
            )
            return
        }
        val queue = newQueue ?: _state.value.queue.ifEmpty { listOf(track) }
        originalQueue = queue
        val index = queue.indexOfFirst { it.id == track.id }.let { if (it >= 0) it else 0 }

        // MediaSessionService owns the background lifecycle/notification. Starting it here
        // ensures playback is promoted to a foreground media service as soon as audio starts.
        runCatching {
            val serviceIntent = Intent(context, SonoraMediaSessionService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }

        _state.value = _state.value.copy(
            currentTrack = track,
            queue = queue,
            queueIndex = index,
            isLoading = true,
            currentPositionMs = 0L,
            durationMs = (track.durationSeconds * 1000L).coerceAtLeast(1000L)
        )

        loadAndPlay(track)
    }

    private fun loadAndPlay(track: Track) {
        if (released) return
        loadJob?.cancel()
        val queue = _state.value.queue.ifEmpty { listOf(track) }
        val startIndex = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

        fun mediaItemFor(item: Track): MediaItem {
            val metadata = MediaMetadata.Builder()
                .setTitle(item.title)
                .setArtist(item.artistName)
                .setAlbumTitle(item.albumName)
                .setArtworkUri(
                    item.artworkUrl.takeIf { it.isNotBlank() }?.let(Uri::parse)
                )
                .build()

            return MediaItem.Builder()
                .setUri(Uri.parse(item.playbackUri))
                .setMediaId(item.id)
                .setMediaMetadata(metadata)
                .build()
        }

        val mediaItems = queue.map(::mediaItemFor)

        loadJob = scope.launch {
            runCatching {
                if (crossfadeSeconds > 0 && exoPlayer.isPlaying) {
                    fadeVolume(1f, 0f, 250)
                }
                if (released) return@runCatching
                exoPlayer.setMediaItems(mediaItems, startIndex, 0L)
                exoPlayer.prepare()
                exoPlayer.volume = if (crossfadeSeconds > 0) 0f else 1f
                exoPlayer.play()
                if (crossfadeSeconds > 0) {
                    fadeVolume(0f, 1f, 350)
                }
            }.onFailure { error ->
                if (!released) {
                    Log.e("SonoraPlayer", "Failed to start playback", error)
                    _state.value = _state.value.copy(
                        isPlaying = false,
                        isLoading = false,
                        errorMessage = "Unable to start this stream. Try another track."
                    )
                }
            }
        }
    }

    private suspend fun fadeVolume(from: Float, to: Float, durationMs: Long) {
        val steps = 10
        val stepTime = durationMs / steps
        val delta = (to - from) / steps
        var vol = from
        for (i in 0 until steps) {
            vol += delta
            exoPlayer.volume = vol.coerceIn(0f, 1f)
            delay(stepTime)
        }
        exoPlayer.volume = to.coerceIn(0f, 1f)
    }

    fun togglePlayPause() {
        if (released) return
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_ENDED) {
                exoPlayer.seekTo(0)
            }
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        if (released) return
        exoPlayer.seekTo(positionMs.coerceAtLeast(0L))
        _state.value = _state.value.copy(currentPositionMs = positionMs)
    }

    fun next() {
        if (released) return
        val currentQueue = _state.value.queue
        if (currentQueue.isEmpty()) return

        val nextIndex = when (_state.value.repeatMode) {
            RepeatMode.ONE -> _state.value.queueIndex
            RepeatMode.ALL -> (_state.value.queueIndex + 1) % currentQueue.size
            RepeatMode.OFF -> {
                val next = _state.value.queueIndex + 1
                if (next < currentQueue.size) next else -1
            }
        }

        if (nextIndex >= 0 && nextIndex < currentQueue.size) {
            val nextTrack = currentQueue[nextIndex]
            _state.value = _state.value.copy(
                currentTrack = nextTrack,
                queueIndex = nextIndex,
                currentPositionMs = 0L,
                durationMs = (nextTrack.durationSeconds * 1000L).coerceAtLeast(1000L)
            )
            loadAndPlay(nextTrack)
        } else {
            exoPlayer.pause()
            exoPlayer.seekTo(0)
            _state.value = _state.value.copy(isPlaying = false, currentPositionMs = 0L)
        }
    }

    fun previous() {
        if (released) return
        // If track played more than 3 seconds, replay from beginning
        if (exoPlayer.currentPosition > 3000L) {
            seekTo(0)
            return
        }

        val currentQueue = _state.value.queue
        if (currentQueue.isEmpty()) return

        val prevIndex = when (_state.value.repeatMode) {
            RepeatMode.ONE -> _state.value.queueIndex
            RepeatMode.ALL -> if (_state.value.queueIndex - 1 < 0) currentQueue.size - 1 else _state.value.queueIndex - 1
            RepeatMode.OFF -> (_state.value.queueIndex - 1).coerceAtLeast(0)
        }

        if (prevIndex >= 0 && prevIndex < currentQueue.size) {
            val prevTrack = currentQueue[prevIndex]
            _state.value = _state.value.copy(
                currentTrack = prevTrack,
                queueIndex = prevIndex,
                currentPositionMs = 0L,
                durationMs = (prevTrack.durationSeconds * 1000L).coerceAtLeast(1000L)
            )
            loadAndPlay(prevTrack)
        }
    }

    private fun handleTrackEnded() {
        when (_state.value.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0)
                exoPlayer.play()
            }
            RepeatMode.ALL, RepeatMode.OFF -> next()
        }
    }

    fun toggleShuffle() {
        val nowShuffle = !_state.value.isShuffleEnabled
        val currentTrack = _state.value.currentTrack
        val currentQueue = _state.value.queue

        if (nowShuffle) {
            val shuffled = currentQueue.toMutableList()
            if (currentTrack != null) {
                shuffled.remove(currentTrack)
                shuffled.shuffle()
                shuffled.add(0, currentTrack)
            } else {
                shuffled.shuffle()
            }
            _state.value = _state.value.copy(
                isShuffleEnabled = true,
                queue = shuffled,
                queueIndex = 0
            )
        } else {
            val original = originalQueue.ifEmpty { currentQueue }
            val newIndex = currentTrack?.let { track -> original.indexOfFirst { it.id == track.id } } ?: 0
            _state.value = _state.value.copy(
                isShuffleEnabled = false,
                queue = original,
                queueIndex = newIndex.coerceAtLeast(0)
            )
        }
    }

    fun toggleRepeat() {
        val nextMode = when (_state.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        exoPlayer.repeatMode = when (nextMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        _state.value = _state.value.copy(repeatMode = nextMode)
    }

    fun addToQueue(track: Track) {
        val updatedQueue = _state.value.queue + track
        _state.value = _state.value.copy(queue = updatedQueue)
    }

    fun playNext(track: Track) {
        val queue = _state.value.queue.toMutableList()
        val insertIndex = (_state.value.queueIndex + 1).coerceAtMost(queue.size)
        queue.add(insertIndex, track)
        _state.value = _state.value.copy(queue = queue)
    }

    fun removeFromQueue(index: Int) {
        val queue = _state.value.queue.toMutableList()
        if (index in queue.indices) {
            queue.removeAt(index)
            val currentIndex = when {
                index < _state.value.queueIndex -> _state.value.queueIndex - 1
                index == _state.value.queueIndex -> _state.value.queueIndex.coerceAtMost(queue.size - 1)
                else -> _state.value.queueIndex
            }
            _state.value = _state.value.copy(queue = queue, queueIndex = currentIndex)
        }
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        val queue = _state.value.queue.toMutableList()
        if (fromIndex in queue.indices && toIndex in queue.indices) {
            val item = queue.removeAt(fromIndex)
            queue.add(toIndex, item)
            val currentTrack = _state.value.currentTrack
            val newIndex = currentTrack?.let { track -> queue.indexOfFirst { it.id == track.id } } ?: 0
            _state.value = _state.value.copy(queue = queue, queueIndex = newIndex)
        }
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _state.value = _state.value.copy(sleepTimerRemainingSec = null)
            return
        }

        var remainingSeconds = minutes * 60
        _state.value = _state.value.copy(sleepTimerRemainingSec = remainingSeconds)

        sleepTimerJob = scope.launch {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds -= 1
                _state.value = _state.value.copy(sleepTimerRemainingSec = remainingSeconds)
            }
            // Sleep timer triggered: smoothly pause
            fadeVolume(exoPlayer.volume, 0f, 1500)
            exoPlayer.pause()
            exoPlayer.volume = 1f
            _state.value = _state.value.copy(isPlaying = false, sleepTimerRemainingSec = null)
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _state.value = _state.value.copy(sleepTimerRemainingSec = null)
    }

    fun setCrossfade(seconds: Int) {
        crossfadeSeconds = seconds
    }

    fun setEqualizerPreset(preset: EqualizerPreset) {
        equalizerHelper.applyPreset(preset)
    }

    fun setLoudnessNormalization(enabled: Boolean) {
        equalizerHelper.setLoudnessEnabled(enabled)
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                if (released) break
                val current = runCatching { exoPlayer.currentPosition }.getOrDefault(0L)
                val duration = if (exoPlayer.duration > 0) exoPlayer.duration else _state.value.durationMs
                val buffered = runCatching { exoPlayer.bufferedPosition }.getOrDefault(0L)
                _state.value = _state.value.copy(
                    currentPositionMs = current,
                    durationMs = duration,
                    bufferedPositionMs = buffered
                )
                delay(200L)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        if (released) return
        released = true
        if (SonoraMediaSessionService.activePlayer === exoPlayer) {
            SonoraMediaSessionService.activePlayer = null
        }
        stopProgressTracker()
        sleepTimerJob?.cancel()
        loadJob?.cancel()
        scope.cancel()
        equalizerHelper.release()
        runCatching { exoPlayer.release() }
    }
}
