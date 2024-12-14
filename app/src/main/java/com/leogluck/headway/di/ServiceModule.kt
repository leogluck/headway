package com.leogluck.headway.di

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.leogluck.headway.FIVE_SECONDS
import com.leogluck.headway.MEDIA_SESSION_TAG
import com.leogluck.headway.TEN_SECONDS
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @OptIn(UnstableApi::class)
    @Provides
    @ServiceScoped
    fun provideExoPlayer(@ApplicationContext context: Context) =
        ExoPlayer.Builder(context).setSeekBackIncrementMs(FIVE_SECONDS)
            .setSeekForwardIncrementMs(TEN_SECONDS)
            .build()

    @Provides
    @ServiceScoped
    fun provideMediaSession(@ApplicationContext context: Context) =
        MediaSessionCompat(context, MEDIA_SESSION_TAG)
}