package com.daugeldauge.kinzhal.sample

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
interface AppComponent {

    fun createArtistsPresenter(): ArtistsPresenter

    fun createAuthPresenter(): AuthPresenter
}
