@file:Suppress("unused")

package com.daugeldauge.kinzhal.sample.graph.network

interface NetworkModule {

    companion object {
        fun provideHttpClient(listOfUnknown: List<*>) = HttpClient()

        fun provideListOfUnknown(): List<*> = listOf(1, 2, 3)
    }

    fun bindLastFm(lastFmApi: LastFmKtorApi): LastFmApi

    fun bindSpotify(spotifyKtorApi: SpotifyKtorApi): SpotifyApi

    fun bindDeezer(deezerApi: DeezerKtorApi): DeezerApi

}
