@file:Suppress("UNUSED_PARAMETER")

package com.daugeldauge.kinzhal.sample.graph

typealias CrazyMap = Map<List<Int?>, Map<String, Map<List<Any>, Map<String, Map<Int, List<*>>>>>>

object AppModule {
    @AppScope
    fun provideContentResolver(@Suppress("UNUSED_PARAMETER") router: Router) = object : ContentResolver {}

    fun provideCrazyMap(): CrazyMap = emptyMap()

    fun provideSimpleMap(): Map<Int, Int> = emptyMap()

    @ScreenId
    fun provideScreenId(crazyMap: CrazyMap, simpleMap: Map<Int, Int> ) = "screen_id"

    @SectionId
    fun provideSectionId() = "section_id"

    @ArtistId
    fun provideArtistId() = "artist_id"

    fun providePlainString() = "plain_string"
}
