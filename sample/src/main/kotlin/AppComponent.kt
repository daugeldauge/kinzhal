package com.daugeldauge.kinzhal.sample


@AppScope
@MainActivityScope
@dagger.Component(modules = [
    NetworkModule::class,
    AppModule::class,
])
interface AppComponent {

    fun createArtistsPresenter(): ArtistsPresenter

}
