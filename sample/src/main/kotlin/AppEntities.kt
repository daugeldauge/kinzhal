@file:Suppress("UNUSED_PARAMETER")

package com.daugeldauge.kinzhal.sample

@JavaxScope
@KinzhalScope
annotation class AppScope

interface ContentResolver

@AppScope
class Database @JavaxInject @KinzhalInject constructor(contentResolver: ContentResolver)

@AppScope
class ArtistImagesStorage @JavaxInject @KinzhalInject constructor()

class AuthPresenter @JavaxInject @KinzhalInject constructor(database: Database, lastFmApi: LastFmApi)
