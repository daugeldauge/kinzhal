@file:Suppress("unused")

package com.daugeldauge.kinzhal.sample.graph.network

interface NetworkModule {

    companion object {
        fun provideHttpClient() = HttpClient()
    }

    fun bindLastFm(lastFmApi: LastFmKtorApi): LastFmApi

    fun bindSpotify(spotifyKtorApi: SpotifyKtorApi): SpotifyApi

    fun bindDeezer(deezerApi: DeezerKtorApi): DeezerApi

}
