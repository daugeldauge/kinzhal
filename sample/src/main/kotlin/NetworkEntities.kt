@file:Suppress("unused", "UNUSED_PARAMETER")

package com.daugeldauge.kinzhal.sample

class HttpClient

interface LastFmApi

interface DeezerApi

interface SpotifyApi

class LastFmKtorApi @JavaxInject @KinzhalInject constructor(client: HttpClient) : LastFmApi

class DeezerKtorApi @JavaxInject @KinzhalInject constructor(client: HttpClient) : DeezerApi

class SpotifyKtorApi @JavaxInject @KinzhalInject constructor(client: HttpClient) : SpotifyApi


