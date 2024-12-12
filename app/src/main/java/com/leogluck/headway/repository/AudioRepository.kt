package com.leogluck.headway.repository

import kotlinx.coroutines.delay
import javax.inject.Inject

class AudioRepository @Inject constructor(
) : IAudioRepository {

    override suspend fun getAudioLinksPlaylist(bookId: String): List<String> {
        // imitating network call
        delay(1000L)
        return listOf(
            "android.resource://com.leogluck.headway/raw/no_conventions",
            "android.resource://com.leogluck.headway/raw/maybe",
            "android.resource://com.leogluck.headway/raw/no_conventions",
            "android.resource://com.leogluck.headway/raw/maybe",
            "https://www.leogluck.com/audio/new_day.wav"
        )
    }
}