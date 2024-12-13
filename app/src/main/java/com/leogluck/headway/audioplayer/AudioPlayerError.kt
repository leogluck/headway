package com.leogluck.headway.audioplayer

import androidx.media3.common.PlaybackException

sealed class AudioPlayerError(
    open val exception: Throwable
) {
    data class PlaybackError(override val exception: PlaybackException) : AudioPlayerError(exception)
    data class PreparationError(override val exception: Throwable) : AudioPlayerError(exception)
}