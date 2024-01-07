@file:Suppress("unused")

package com.daugeldauge.kinzhal.sample.graph.network

interface NetworkModule {

    companion object {
        @HttpClientScope
        fun provideHttpClient(@Suppress("UNUSED_PARAMETER") listOfUnknown: List<*>) = HttpClient()

        fun provideListOfUnknown(): List<*> = listOf(1, 2, 3)

        @HttpClientScope
        fun provideDiscogsImpl(assistedFactory: DiscogsKtorApiFactory): DiscogsKtorApi {
            return assistedFactory.create(
                apiKey = "123",
                userAgent = "Kinzhal",
                providerApiKey = { "provider 123" },
                lazyUserAgent = lazy { "Lazy Kinzhal" },
            )
        }
    }

    fun bindLastFm(lastFmApi: LastFmKtorApi): LastFmApi

    fun bindSpotify(spotifyKtorApi: SpotifyKtorApi): SpotifyApi

    fun bindDeezer(deezerApi: DeezerKtorApi): DeezerApi

    fun bindDeezer(discogsKtorApi: DiscogsKtorApi): DiscogsApi
}
