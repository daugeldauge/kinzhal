package com.daugeldauge.kinzhal.sample

@dagger.Module
object AppModule {
    @AppScope
    @dagger.Provides
    fun provideContentResolver() = object : ContentResolver {}
}
