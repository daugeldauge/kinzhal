package com.daugeldauge.kinzhal.sample.graph

import com.daugeldauge.kinzhal.annotations.Component
import com.daugeldauge.kinzhal.sample.graph.network.NetworkModule

interface SuperComponent {
    fun createArtistsPresenter(): ArtistsPresenter
}

@AppScope
@MainActivityScope
@Component(modules = [
    NetworkModule::class,
    AppModule::class,
], dependencies = [
    AppDependencies::class,
])
interface AppComponent : SuperComponent {

    fun createAuthPresenter(): AuthPresenter

    val router: Router
}

interface AppDependencies {
    val application: Application
}
