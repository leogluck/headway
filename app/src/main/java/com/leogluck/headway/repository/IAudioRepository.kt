package com.leogluck.headway.repository

import com.leogluck.headway.model.BookInfo

interface IAudioRepository {
    suspend fun getBookInfo(bookId: String): BookInfo
}