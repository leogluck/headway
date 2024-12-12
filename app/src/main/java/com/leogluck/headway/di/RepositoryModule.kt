package com.leogluck.headway.di

import com.leogluck.headway.repository.AudioRepository
import com.leogluck.headway.repository.IAudioRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideAudioRepository(): IAudioRepository {
        return AudioRepository()
    }
}