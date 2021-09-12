@file:Suppress("UNUSED_PARAMETER")

package com.daugeldauge.kinzhal.sample

import com.daugeldauge.kinzhal.Qualifier
import com.daugeldauge.kinzhal.sample.network.LastFmApi

@JavaxScope
@KinzhalScope
annotation class AppScope

class Application

interface ContentResolver

@AppScope
class Database @JavaxInject @KinzhalInject constructor(contentResolver: ContentResolver)

@AppScope
class ArtistImagesStorage @JavaxInject @KinzhalInject constructor()

@Qualifier
annotation class ScreenId

class AuthPresenter @JavaxInject @KinzhalInject constructor(database: Database, lastFmApi: LastFmApi, @ScreenId screenId: String)
