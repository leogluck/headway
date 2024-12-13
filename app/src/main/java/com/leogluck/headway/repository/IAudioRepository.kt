package com.leogluck.headway.repository

import com.leogluck.headway.model.BookInfo

interface IAudioRepository {
    suspend fun getAudioLinksPlaylist(bookId: String): BookInfo
}