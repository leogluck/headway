package com.leogluck.headway.audioplayer

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.support.v4.media.session.MediaSessionCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.leogluck.headway.Launcher
import com.leogluck.headway.NOTIFICATION_ID
import com.leogluck.headway.ONE_SECOND_DELAY
import com.leogluck.headway.notification.createNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class AudioPlayerService : Service(), IAudioPlayer {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var mediaSession: MediaSessionCompat

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var progressUpdateJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState = _playbackState.asStateFlow()

    private val _errors = MutableSharedFlow<AudioPlayerError>()
    val errors = _errors.asSharedFlow()


    private val binder = AudioBinder()

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_READY -> {
                    _playbackState.update {
                        it.copy(
                            duration = exoPlayer.duration,
                            currentTrackNumber = exoPlayer.currentMediaItemIndex.plus(1),
                            totalTracks = exoPlayer.mediaItemCount
                        )
                    }
                }

                Player.STATE_ENDED -> {
                    _playbackState.update { it.copy(isPlaying = false) }
                    stopProgressUpdates()
                    updateForegroundNotification()
                }

                Player.STATE_BUFFERING -> {}

                Player.STATE_IDLE -> {}
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) {
                Launcher.startForegroundService(
                    this@AudioPlayerService, createNotification(isPlaying = true)
                )
                startProgressUpdates()
            } else {
                updateForegroundNotification()
                stopProgressUpdates()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            scope.launch {
                _errors.emit(AudioPlayerError.PlaybackError(error))
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        exoPlayer.addListener(playerListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> exoPlayer.play()
            ACTION_PAUSE -> exoPlayer.pause()
            ACTION_SKIP_TO_NEXT -> exoPlayer.seekToNext()
            ACTION_SKIP_TO_PREVIOUS -> exoPlayer.seekToPrevious()
            ACTION_STOP_SERVICE -> {
                exoPlayer.stop()
                stopForeground(true)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        exoPlayer.release()
        mediaSession.release()
        stopForeground(true)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = binder

    override fun prepare(uris: List<Uri>) {
        runCatching {
            uris.map { uri ->
                MediaItem.fromUri(uri)
            }.also { mediaItems ->
                exoPlayer.setMediaItems(mediaItems)
                exoPlayer.prepare()
            }
        }.onFailure {
            scope.launch {
                _errors.emit(AudioPlayerError.PreparationError(it))
            }
        }
    }

    override fun play() = exoPlayer.play()

    override fun pause() = exoPlayer.pause()

    override fun seekForward() = exoPlayer.seekForward()

    override fun seekBackward() = exoPlayer.seekBack()

    override fun skipToNext() = exoPlayer.seekToNext()

    override fun skipToPrevious() = exoPlayer.seekToPrevious()

    override fun seekToPosition(position: Long) {
        exoPlayer.seekTo(position)
        _playbackState.update { it.copy(currentPosition = position) }
    }

    override fun setPlaybackSpeed(speed: Float) = exoPlayer.setPlaybackSpeed(speed)

    private fun updateForegroundNotification() = notificationManager.notify(
        NOTIFICATION_ID, createNotification(_playbackState.value.isPlaying)
    )


    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressUpdateJob = scope.launch {
            while (isActive) {
                withContext(Dispatchers.Main) {
                    _playbackState.update { it.copy(currentPosition = exoPlayer.currentPosition) }
                }
                delay(ONE_SECOND_DELAY)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    inner class AudioBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    companion object {
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_SKIP_TO_NEXT = "ACTION_SKIP_TO_NEXT"
        const val ACTION_SKIP_TO_PREVIOUS = "ACTION_SKIP_TO_PREVIOUS"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    }
}