package com.leogluck.headway.bookplayer

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leogluck.headway.audioplayer.PlaybackState
import com.leogluck.headway.bookplayer.Event.OnBindAudioPlayer
import com.leogluck.headway.bookplayer.Event.PlayPauseClicked
import com.leogluck.headway.bookplayer.Event.PlaybackStateChanged
import com.leogluck.headway.bookplayer.Event.Seek
import com.leogluck.headway.bookplayer.Event.SeekBackwardClicked
import com.leogluck.headway.bookplayer.Event.SeekForwardClicked
import com.leogluck.headway.bookplayer.Event.SetData
import com.leogluck.headway.bookplayer.Event.SkipNextClicked
import com.leogluck.headway.bookplayer.Event.SkipPreviousClicked
import com.leogluck.headway.millisToPosition
import com.leogluck.headway.positionToMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookPlayerViewModel @Inject constructor(
//    private val audioRepository: AudioRepository
) : ViewModel() {

    private val booksAudio = mutableListOf<Uri>()

    private val _uiState = MutableStateFlow(
        ScreenState(
            isPlaying = false,
            currentPosition = 40.5f,
            totalDuration = 350.7f,
            currentTrackNumber = 2,
            totalTracks = 10,
            playbackSpeed = 1
        )
    )
    val uiState: StateFlow<ScreenState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<Effect>()
    val effects: SharedFlow<Effect> = _effects.asSharedFlow()

    fun onEvent(event: Event) {
        when (event) {
            SetData -> loadAudio()
            OnBindAudioPlayer -> prepareAudioPlayer()
            PlayPauseClicked -> handlePlayPauseEvent()
            SeekForwardClicked -> seekForward()
            SeekBackwardClicked -> seekBackward()
            is Seek -> seekMedia(event.position)
            is PlaybackStateChanged -> handlePlaybackStateChanged(event.playbackState)
            SkipNextClicked -> skipNext()
            SkipPreviousClicked -> skipPrevious()
        }
    }

    private fun loadAudio() {
        booksAudio.add(Uri.parse("android.resource://com.leogluck.headway/raw/no_conventions"))
        booksAudio.add(Uri.parse("android.resource://com.leogluck.headway/raw/maybe"))
        booksAudio.add(Uri.parse("android.resource://com.leogluck.headway/raw/no_conventions"))
        booksAudio.add(Uri.parse("android.resource://com.leogluck.headway/raw/maybe"))


    }

    private fun prepareAudioPlayer() {
        if (booksAudio.isNotEmpty()) {
            viewModelScope.launch {
                _effects.emit(Effect.Prepare(booksAudio))
            }
            booksAudio.clear()
        }
    }

    private fun seekBackward() {
        viewModelScope.launch {
            _effects.emit(Effect.SeekBackward)
        }
    }

    private fun seekForward() {
        viewModelScope.launch {
            _effects.emit(Effect.SeekForward)
        }
    }

    private fun skipNext() {
        viewModelScope.launch {
            _effects.emit(Effect.SkipNext)
        }
    }

    private fun skipPrevious() {
        viewModelScope.launch {
            _effects.emit(Effect.SkipPrevious)
        }
    }

    private fun seekMedia(position: Float) {
        _uiState.update {
            it.copy(currentPosition = position)
        }
        seekMedia(position.positionToMillis())
    }

    private fun handlePlaybackStateChanged(playbackState: PlaybackState) {
        _uiState.update {
            it.copy(
                isPlaying = playbackState.isPlaying,
                currentPosition = playbackState.currentPosition.millisToPosition(),
                totalDuration = playbackState.duration.millisToPosition(),
                currentTrackNumber = playbackState.currentTrackNumber,
                totalTracks = playbackState.totalTracks
            )
        }
    }

    private fun handlePlayPauseEvent() {
        if (_uiState.value.isPlaying) {
            pauseMedia()
        } else {
            playMedia()
        }
    }

    private fun playMedia() {
        viewModelScope.launch {
            _effects.emit(
                Effect.Play
            )
        }
    }

    private fun pauseMedia() {
        viewModelScope.launch {
            _effects.emit(Effect.Pause)
        }
    }

    private fun seekMedia(position: Long) {
        viewModelScope.launch {
            _effects.emit(Effect.Seek(position))
        }
    }
}