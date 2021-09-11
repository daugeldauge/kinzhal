@file:Suppress("UNUSED_PARAMETER")

package com.daugeldauge.kinzhal.sample

import javax.inject.Inject as JavaxInject

@javax.inject.Scope
annotation class AppScope

interface ContentResolver

@AppScope
class Database @JavaxInject constructor(contentResolver: ContentResolver)

@AppScope
class ArtistImagesStorage @JavaxInject constructor()

class AuthPresenter @JavaxInject constructor(database: Database, lastFmApi: LastFmApi)
