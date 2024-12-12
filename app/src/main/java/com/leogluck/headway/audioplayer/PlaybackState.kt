package com.leogluck.headway.audioplayer

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val currentTrackNumber: Int = 0,
    val totalTracks: Int = 0
)
