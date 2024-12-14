package com.leogluck.headway.bookplayer

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leogluck.headway.audioplayer.PlaybackState
import com.leogluck.headway.bookplayer.Event.BindAudioPlayer
import com.leogluck.headway.bookplayer.Event.DismissBottomSheet
import com.leogluck.headway.bookplayer.Event.DismissError
import com.leogluck.headway.bookplayer.Event.PlayPauseClicked
import com.leogluck.headway.bookplayer.Event.PlaybackError
import com.leogluck.headway.bookplayer.Event.PlaybackSpeedClicked
import com.leogluck.headway.bookplayer.Event.PlaybackSpeedSelected
import com.leogluck.headway.bookplayer.Event.PlaybackStateChanged
import com.leogluck.headway.bookplayer.Event.Seek
import com.leogluck.headway.bookplayer.Event.SeekBackwardClicked
import com.leogluck.headway.bookplayer.Event.SeekForwardClicked
import com.leogluck.headway.bookplayer.Event.SetData
import com.leogluck.headway.bookplayer.Event.SkipNextClicked
import com.leogluck.headway.bookplayer.Event.SkipPreviousClicked
import com.leogluck.headway.di.IoDispatcher
import com.leogluck.headway.millisToPosition
import com.leogluck.headway.positionToMillis
import com.leogluck.headway.repository.IAudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BookPlayerViewModel @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val audioRepository: IAudioRepository
) : ViewModel() {

    private var bookInfoFetchingJob: Job? = null
    private var bookInfoDeferred: Deferred<List<Uri>>? = null

    private val _screenState = MutableStateFlow(ScreenState())
    val screenState = _screenState.asStateFlow()

    private val _effects = MutableSharedFlow<Effect>()
    val effects: SharedFlow<Effect> = _effects.asSharedFlow()

    fun onEvent(event: Event) {
        when (event) {
            is SetData -> loadAudio(event.bookId)
            BindAudioPlayer -> prepareAudioPlayer()
            PlayPauseClicked -> handlePlayPauseEvent()
            SeekForwardClicked -> seekForward()
            SeekBackwardClicked -> seekBackward()
            is Seek -> seekMedia(event.position)
            is PlaybackStateChanged -> handlePlaybackStateChanged(event.playbackState)
            SkipNextClicked -> skipNext()
            SkipPreviousClicked -> skipPrevious()
            PlaybackSpeedClicked -> showChoosePlaybackSpeedBottomSheet()
            is PlaybackSpeedSelected -> setPlaybackSpeed(event.speed)
            DismissBottomSheet -> dismissBottomSheet()
            is PlaybackError -> handleErrors(event.error.exception.message)
            DismissError -> dismissError()
        }
    }

    private fun loadAudio(bookId: String) {
        bookInfoFetchingJob = viewModelScope.launch {
            bookInfoDeferred = async {
                runCatching {
                    withContext(ioDispatcher) { audioRepository.getBookInfo(bookId) }
                }.onSuccess { bookInfo ->
                    _screenState.update {
                        it.copy(
                            bitmapResourceId = bookInfo.bitmapResourceId
                        )
                    }
                }.onFailure {
                    handleErrors(it.message)
                }.map { bookInfo ->
                    bookInfo.playlist.map { Uri.parse(it) }
                }.getOrElse { emptyList() }
            }
        }
    }

    private fun prepareAudioPlayer() {
        viewModelScope.launch {
            bookInfoDeferred?.await()?.let {
                _effects.emit(Effect.Prepare(it))
            }
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

    private fun showChoosePlaybackSpeedBottomSheet() {
        _screenState.update {
            it.copy(isBottomSheetVisible = true)
        }
    }

    private fun dismissBottomSheet() {
        _screenState.update {
            it.copy(isBottomSheetVisible = false)
        }
    }

    private fun setPlaybackSpeed(speed: Float) {
        _screenState.update {
            it.copy(isBottomSheetVisible = false, playbackSpeed = speed)
        }

        viewModelScope.launch {
            _effects.emit(Effect.SetPlaybackSpeed(speed))
        }
    }

    private fun seekMedia(position: Float) {
        _screenState.update {
            it.copy(currentPosition = position)
        }
        seekMedia(position.positionToMillis())
    }

    private fun handlePlaybackStateChanged(playbackState: PlaybackState) {
        _screenState.update {
            it.copy(
                isPlaying = playbackState.isPlaying,
                currentPosition = playbackState.currentPosition.millisToPosition(),
                totalDuration = playbackState.duration.millisToPosition(),
                currentTrackNumber = playbackState.currentTrackNumber,
                totalTracks = playbackState.totalTracks
            )
        }
    }

    private fun handleErrors(errorMessage: String?) {
        _screenState.update { it.copy(errorMessage = errorMessage) }
    }

    private fun dismissError() {
        _screenState.update { it.copy(errorMessage = null) }
    }

    private fun handlePlayPauseEvent() {
        if (_screenState.value.isPlaying) {
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

    override fun onCleared() {
        bookInfoFetchingJob?.cancel()
        super.onCleared()
    }
}