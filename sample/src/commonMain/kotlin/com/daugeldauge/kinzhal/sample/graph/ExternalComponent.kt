package com.daugeldauge.kinzhal.sample.graph

import com.daugeldauge.kinzhal.annotations.Component
import com.daugeldauge.kinzhal.sample.graph.network.DeezerApi
import com.daugeldauge.kinzhal.sample.graph.network.HttpClientScope
import com.daugeldauge.kinzhal.sample.graph.network.NetworkModule

@HttpClientScope
@Component(modules = [NetworkModule::class, ExternalModule::class])
interface ExternalComponent {
    val musicPlayer: ExternalMusicPlayer
}

object ExternalModule {
    fun provideType(@Suppress("UNUSED_PARAMETER") deezerApi: DeezerApi) = ExternalMusicPlayer()
}

class ExternalMusicPlayer
