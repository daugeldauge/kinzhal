@file:Suppress("unused", "UNUSED_PARAMETER")

package com.daugeldauge.kinzhal.sample

class HttpClient

interface LastFmApi

interface DeezerApi

interface SpotifyApi

class LastFmKtorApi @javax.inject.Inject constructor(client: HttpClient) : LastFmApi

class DeezerKtorApi @javax.inject.Inject constructor(client: HttpClient) : DeezerApi

class SpotifyKtorApi @javax.inject.Inject constructor(client: HttpClient) : SpotifyApi


