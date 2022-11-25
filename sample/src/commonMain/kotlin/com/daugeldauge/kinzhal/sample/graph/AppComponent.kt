package com.daugeldauge.kinzhal.sample.graph

import com.daugeldauge.kinzhal.annotations.Component
import com.daugeldauge.kinzhal.sample.graph.network.*
import com.daugeldauge.kinzhal.sample.graph.network.NetworkModule

interface SuperComponent {
    fun createArtistsPresenter(): ArtistsPresenter
}

typealias VersionCode = Int
typealias Versions = Map<VersionCode, String>

@AppScope
@MainActivityScope
@HttpClientScope
@Component(modules = [
    NetworkModule::class,
    AppModule::class,
    SuperComponentBindingModule::class,
], dependencies = [
    AppDependencies::class,
    ExternalComponent::class,
])
interface AppComponent : SuperComponent {

    fun createAuthPresenter(): AuthPresenter

    val router: Router

    val versions: Versions

    val superComponent: SuperComponent
}

interface AppDependencies {
    val application: Application
    val versions: Map<Int, String>
}

interface SuperComponentBindingModule {
    fun bindAppComponentAsSuperComponent(component: AppComponent): SuperComponent
}