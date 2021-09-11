@file:Suppress("unused")

package com.daugeldauge.kinzhal.sample

@DaggerModule
interface NetworkModule {

    companion object {
        @DaggerProvides
        fun provideHttpClient() = HttpClient()
    }

    @DaggerBinds
    fun bindLastFm(lastFmApi: LastFmKtorApi): LastFmApi

    @DaggerBinds
    fun bindSpotify(spotifyKtorApi: SpotifyKtorApi): SpotifyApi

    @DaggerBinds
    fun bindDeezer(deezerApi: DeezerKtorApi): DeezerApi

}
