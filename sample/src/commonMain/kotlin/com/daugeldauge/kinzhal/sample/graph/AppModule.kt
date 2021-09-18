@file:Suppress("UNUSED_PARAMETER")

package com.daugeldauge.kinzhal.sample.graph

object AppModule {
    @AppScope
    fun provideContentResolver(@Suppress("UNUSED_PARAMETER") router: Router) = object : ContentResolver {}

    @ScreenId
    fun provideScreenId() = "screen_id"
}
