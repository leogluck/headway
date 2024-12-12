package com.leogluck.headway.repository

interface IAudioRepository {
    suspend fun getAudioLinksPlaylist(bookId: String): List<String>
}