package com.leogluck.headway.repository

import com.leogluck.headway.R
import com.leogluck.headway.model.BookInfo
import kotlinx.coroutines.delay
import javax.inject.Inject

class AudioRepository @Inject constructor(
) : IAudioRepository {

    override suspend fun getBookInfo(bookId: String): BookInfo {
        // imitating network call
        delay(1000L)
        return BookInfo(
            id = bookId,
            bitmapResourceId = R.drawable.book_cover,
            playlist = listOf(
                "android.resource://com.leogluck.headway/raw/no_conventions",
                "android.resource://com.leogluck.headway/raw/maybe",
                "android.resource://com.leogluck.headway/raw/no_conventions",
                "android.resource://com.leogluck.headway/raw/maybe"
            )
        )
    }
}