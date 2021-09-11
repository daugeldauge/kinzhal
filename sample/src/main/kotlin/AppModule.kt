package com.daugeldauge.kinzhal.sample

@DaggerModule
object AppModule {
    @AppScope
    @DaggerProvides
    fun provideContentResolver() = object : ContentResolver {}
}
