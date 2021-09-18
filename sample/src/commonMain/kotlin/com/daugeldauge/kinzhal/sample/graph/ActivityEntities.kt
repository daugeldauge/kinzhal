@file:Suppress("unused", "UNUSED_PARAMETER")

package com.daugeldauge.kinzhal.sample.graph

import com.daugeldauge.kinzhal.annotations.Inject
import com.daugeldauge.kinzhal.annotations.Scope
import com.daugeldauge.kinzhal.sample.graph.network.DeezerApi
import com.daugeldauge.kinzhal.sample.graph.network.SpotifyApi

@Scope
annotation class MainActivityScope

@MainActivityScope
class Router @Inject constructor(application: Application, versions: Versions)

class ArtistsPresenter @Inject constructor(
    private val database: Database,
    private val artistImagesStorage: ArtistImagesStorage,
    private val deezer: DeezerApi,
    private val spotify: SpotifyApi,
    private val router: Router,
)
