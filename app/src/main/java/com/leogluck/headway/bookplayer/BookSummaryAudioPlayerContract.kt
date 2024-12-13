package com.leogluck.headway.bookplayer

import android.net.Uri
import com.leogluck.headway.R
import com.leogluck.headway.audioplayer.AudioPlayerError
import com.leogluck.headway.audioplayer.PlaybackState

data class ScreenState(
    val isPlaying: Boolean = false,
    val currentPosition: Float = 0F,
    val totalDuration: Float = 0F,
    val currentTrackNumber: Int = 0,
    val totalTracks: Int = 0,
    val playbackSpeed: Int = 1,
    val bitmapResourceId: Int? = null,
    val errorMessage: String? = null
)

sealed interface Event {
    data class SetData(val bookId: String) : Event
    data object BindAudioPlayer : Event
    data object PlayPauseClicked : Event
    data object SeekForwardClicked : Event
    data object SeekBackwardClicked : Event
    data object SkipNextClicked : Event
    data object SkipPreviousClicked : Event
    data class Seek(val position: Float) : Event
    data class PlaybackStateChanged(val playbackState: PlaybackState) : Event
    class PlaybackError(val error: AudioPlayerError) : Event
    data object DismissError : Event
}

sealed interface Effect {
    data class Prepare(val uri: List<Uri>) : Effect
    data object Play : Effect
    data object Pause : Effect
    data object SkipNext : Effect
    data object SkipPrevious : Effect
    data object SeekForward : Effect
    data object SeekBackward : Effect
    data class Seek(val position: Long) : Effect
}