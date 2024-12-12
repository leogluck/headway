package com.leogluck.headway.audioplayer

import android.net.Uri

interface IAudioPlayer {
    fun prepare(uris: List<Uri>)
    fun play()
    fun pause()
    fun skipToNext()
    fun skipToPrevious()
    fun seekToPosition(position: Long)
    fun seekForward()
    fun seekBackward()
}