@file:Suppress("unused", "UNUSED_PARAMETER")

package com.daugeldauge.kinzhal.sample

@javax.inject.Scope
annotation class MainActivityScope

@MainActivityScope
class Router @javax.inject.Inject constructor()

class ArtistsPresenter @javax.inject.Inject constructor(
    private val database: Database,
    private val artistImagesStorage: ArtistImagesStorage,
    private val deezer: DeezerApi,
    private val spotify: SpotifyApi,
    private val router: Router,
)
