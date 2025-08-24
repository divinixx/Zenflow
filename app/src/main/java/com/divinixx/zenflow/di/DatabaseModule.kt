package com.divinixx.zenflow.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    // Database module kept for future use if needed
    // Currently no database dependencies required for Zenflow Remote
}
