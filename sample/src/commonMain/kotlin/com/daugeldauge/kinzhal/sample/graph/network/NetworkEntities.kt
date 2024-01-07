@file:Suppress("unused", "UNUSED_PARAMETER")

package com.daugeldauge.kinzhal.sample.graph.network

import com.daugeldauge.kinzhal.annotations.Assisted
import com.daugeldauge.kinzhal.annotations.AssistedFactory
import com.daugeldauge.kinzhal.annotations.AssistedInject
import com.daugeldauge.kinzhal.annotations.Inject
import com.daugeldauge.kinzhal.annotations.Scope
import com.daugeldauge.kinzhal.sample.graph.Application
import com.daugeldauge.kinzhal.sample.graph.Versions

@Scope
annotation class HttpClientScope

class HttpClient

interface LastFmApi

interface DeezerApi

interface SpotifyApi

interface DiscogsApi

class LastFmKtorApi @Inject constructor(client: HttpClient) : LastFmApi

class DeezerKtorApi @Inject constructor(client: HttpClient) : DeezerApi

class SpotifyKtorApi @Inject constructor(client: HttpClient) : SpotifyApi

class DiscogsKtorApi @AssistedInject constructor(
    application: Application,
    client: Lazy<HttpClient>,
    versionsProvider: () -> Versions,
    @Assisted apiKey: String,
    @Assisted userAgent: String,
    @Assisted providerApiKey: () -> String,
    @Assisted lazyUserAgent: Lazy<String>,
) : DiscogsApi

@AssistedFactory
interface DiscogsKtorApiFactory {
    fun create(
        apiKey: String,
        userAgent: String,
        providerApiKey: () -> String,
        lazyUserAgent: Lazy<String>,
    ): DiscogsKtorApi
}
