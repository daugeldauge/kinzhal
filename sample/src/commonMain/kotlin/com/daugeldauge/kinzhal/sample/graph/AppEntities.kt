@file:Suppress("UNUSED_PARAMETER")

package com.daugeldauge.kinzhal.sample.graph

import com.daugeldauge.kinzhal.annotations.Inject
import com.daugeldauge.kinzhal.annotations.Qualifier
import com.daugeldauge.kinzhal.annotations.Scope
import com.daugeldauge.kinzhal.sample.graph.network.LastFmApi

@Scope
annotation class AppScope

class Application

interface ContentResolver

@AppScope
class Database @Inject constructor(contentResolver: ContentResolver)

@AppScope
class ArtistImagesStorage @Inject constructor()

@Qualifier
annotation class ScreenId

@Qualifier
annotation class SectionId

@Qualifier
annotation class ArtistId

class AuthPresenter @Inject constructor(
    database: Database,
    lastFmApi: LastFmApi,
    @ScreenId screenId: String,
    @SectionId sectionId: String,
    @ArtistId artistId: String,
    plainString: String,
)
