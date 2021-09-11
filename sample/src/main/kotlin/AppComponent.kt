package com.daugeldauge.kinzhal.sample

import com.daugeldauge.kinzhal.sample.network.NetworkModule

interface SuperComponent {
    fun createArtistsPresenter(): ArtistsPresenter
}

@AppScope
@MainActivityScope
@DaggerComponent(modules = [
    NetworkModule::class,
    AppModule::class,
])
@KinzhalComponent(modules = [
    NetworkModule::class,
    AppModule::class,
])
interface AppComponent : SuperComponent {

    fun createAuthPresenter(): AuthPresenter

    val router: Router
}
