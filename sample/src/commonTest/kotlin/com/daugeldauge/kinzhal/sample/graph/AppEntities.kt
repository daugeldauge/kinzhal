@file:Suppress("UNUSED_PARAMETER")

package com.daugeldauge.kinzhal.sample.graph

import com.daugeldauge.kinzhal.Inject
import com.daugeldauge.kinzhal.Qualifier
import com.daugeldauge.kinzhal.Scope
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

class AuthPresenter @Inject constructor(database: Database, lastFmApi: LastFmApi, @ScreenId screenId: String)
