@file:Suppress("unused")

package com.daugeldauge.kinzhal.sample.graph.network

interface NetworkModule {

    companion object {
        @HttpClientScope
        fun provideHttpClient(@Suppress("UNUSED_PARAMETER") listOfUnknown: List<*>) = HttpClient()

        fun provideListOfUnknown(): List<*> = listOf(1, 2, 3)
    }

    fun bindLastFm(lastFmApi: LastFmKtorApi): LastFmApi

    fun bindSpotify(spotifyKtorApi: SpotifyKtorApi): SpotifyApi

    fun bindDeezer(deezerApi: DeezerKtorApi): DeezerApi

}
