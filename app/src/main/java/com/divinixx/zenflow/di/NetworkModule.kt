package com.divinixx.zenflow.di

import android.content.Context
import com.divinixx.zenflow.network.KtorWebSocketManager
import com.divinixx.zenflow.network.NetworkDiscoveryManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    
    @Provides
    @Singleton
    fun provideNetworkDiscoveryManager(@ApplicationContext context: Context): NetworkDiscoveryManager {
        return NetworkDiscoveryManager(context)
    }
}
