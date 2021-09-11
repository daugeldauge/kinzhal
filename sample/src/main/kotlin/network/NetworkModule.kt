@file:Suppress("unused")

package com.daugeldauge.kinzhal.sample.network

import com.daugeldauge.kinzhal.sample.DaggerBinds
import com.daugeldauge.kinzhal.sample.DaggerModule
import com.daugeldauge.kinzhal.sample.DaggerProvides

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
