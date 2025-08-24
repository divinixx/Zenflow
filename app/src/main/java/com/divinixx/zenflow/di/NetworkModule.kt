package com.divinixx.zenflow.di

import com.divinixx.zenflow.network.KtorWebSocketManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideKtorWebSocketManager(): KtorWebSocketManager {
        return KtorWebSocketManager()
    }
}
