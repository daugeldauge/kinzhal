@file:Suppress("unused", "UNUSED_PARAMETER")

package com.daugeldauge.kinzhal.sample.graph.network

import com.daugeldauge.kinzhal.annotations.Inject

class HttpClient

interface LastFmApi

interface DeezerApi

interface SpotifyApi

class LastFmKtorApi @Inject constructor(client: HttpClient) : LastFmApi

class DeezerKtorApi @Inject constructor(client: HttpClient) : DeezerApi

class SpotifyKtorApi @Inject constructor(client: HttpClient) : SpotifyApi


