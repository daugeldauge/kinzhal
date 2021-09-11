@file:Suppress("unused", "UNUSED_PARAMETER")

package com.daugeldauge.kinzhal.sample

@JavaxScope
@KinzhalScope
annotation class MainActivityScope

@MainActivityScope
class Router @JavaxInject @KinzhalInject constructor()

class ArtistsPresenter @JavaxInject @KinzhalInject constructor(
    private val database: Database,
    private val artistImagesStorage: ArtistImagesStorage,
    private val deezer: DeezerApi,
    private val spotify: SpotifyApi,
    private val router: Router,
)
