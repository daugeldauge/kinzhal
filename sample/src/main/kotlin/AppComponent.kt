package com.daugeldauge.kinzhal.sample

import com.daugeldauge.kinzhal.sample.network.*

interface SuperComponent {
    fun createArtistsPresenter(): ArtistsPresenter
}

@AppScope
@MainActivityScope
@DaggerComponent(modules = [
    NetworkModule::class,
    AppModule::class,
], dependencies = [
    AppDependencies::class,
])
@KinzhalComponent(modules = [
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


class AppComponentImpl(private val appDependencies: AppDependencies) : AppComponent {

    private val httpClientProvider = HttpClientFactory()
    private val routerLazy = lazy(RouterFactory(appDependencies::application))
    private val contentResolverLazy = lazy(ContentResolverFactory(routerLazy::value))
    private val contentResolverProvider = contentResolverLazy::value
    private val databaseLazy = lazy(DatabaseFactory(contentResolverProvider))
    private val lastFmKtorApiProvider = LastFmKtorApiFactory(httpClientProvider)
    private val authPresenterProvider = AuthPresenterFactory(databaseLazy::value, lastFmKtorApiProvider)
    private val deezerKtorApiProvider = DeezerKtorApiFactory(httpClientProvider)
    private val deezerApiProvider = deezerKtorApiProvider
    private val spotifyProvider = SpotifyKtorApiFactory(httpClientProvider)
    private val artistImagesStorageLazy = lazy(ArtistImagesStorageFactory())
    private val artistsPresenterProvider = ArtistsPresenterFactory(databaseLazy::value, artistImagesStorageLazy::value, deezerApiProvider, spotifyProvider, routerLazy::value)

    override val router: Router
        get() = (routerLazy::value)()

    override fun createAuthPresenter(): AuthPresenter {
        return authPresenterProvider()
    }

    override fun createArtistsPresenter(): ArtistsPresenter {
        return artistsPresenterProvider()
    }
}
