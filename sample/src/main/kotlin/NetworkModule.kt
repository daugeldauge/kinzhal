@file:Suppress("unused")

package com.daugeldauge.kinzhal.sample

@dagger.Module
interface NetworkModule {

    companion object {
        @dagger.Provides
        fun provideHttpClient() = HttpClient()
    }

    @dagger.Binds
    fun bindLastFm(lastFmApi: LastFmKtorApi): LastFmApi

    @dagger.Binds
    fun bindSpotify(spotifyKtorApi: SpotifyKtorApi): SpotifyApi

    @dagger.Binds
    fun bindDeezer(deezerApi: DeezerKtorApi): DeezerApi

}
