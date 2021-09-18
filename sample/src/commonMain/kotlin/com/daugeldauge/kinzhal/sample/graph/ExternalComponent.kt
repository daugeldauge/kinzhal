package com.daugeldauge.kinzhal.sample.graph

import com.daugeldauge.kinzhal.annotations.Component
import com.daugeldauge.kinzhal.sample.graph.network.DeezerApi
import com.daugeldauge.kinzhal.sample.graph.network.HttpClientScope
import com.daugeldauge.kinzhal.sample.graph.network.NetworkModule

@HttpClientScope
@Component(modules = [NetworkModule::class])
interface ExternalComponent {
    val musicPlayer: ExternalMusicPlayer
}

interface ExternalModule {
    fun provideType(deezerApi: DeezerApi) = ExternalMusicPlayer()
}

class ExternalMusicPlayer
