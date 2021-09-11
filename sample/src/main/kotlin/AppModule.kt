package com.daugeldauge.kinzhal.sample

@DaggerModule
object AppModule {
    @AppScope
    @DaggerProvides
    fun provideContentResolver(@Suppress("UNUSED_PARAMETER") router: Router) = object : ContentResolver {}
}
