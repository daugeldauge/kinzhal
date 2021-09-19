[![Maven Central](https://img.shields.io/maven-central/v/com.daugeldauge.kinzhal/kinzhal-processor?color=blue)](https://search.maven.org/artifact/kinzhal/kinzhal-processor)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# Kinzhal

Kinzhal is a Kotlin Multiplatform library for compile-time dependency injection. The goal is to emulate basic features of [Dagger](https://dagger.dev/) to achieve similar experience in Kotlin Multiplatform projects

Kinzhal is based on [Kotlin Symbol Processing](https://github.com/google/ksp) (KSP) — the API for lightweight compiler plugins. You'll need to set up KSP in your project to use Kinzhal

## Setup

Add KSP to your plugins section:
```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.5.30-1.0.0"
    kotlin("multiplatform")
    // ...
}
```

Apply Kinzhal KSP processor: 
```kotlin
dependencies {
    ksp("com.daugeldauge.kinzhal:kinzhal-processor:$kinzhalVersion")
}
```

Add compile-only `kinzhal-annotations` dependency to your common source set:
```kotlin
kotlin {    
    sourceSets {
        getByName("commonMain") {
            dependencies {
                compileOnly("com.daugeldauge.kinzhal:kinzhal-annotations:$kinzhalVersion")
                // ...
            }
        }

        // Optional workaround for intellij-based IDEs to see generated code. This probably will be fixed someday in KSP plugin.
        // You can replace `jvmName` with any of your target source sets. After the source set is built IDE will start to recognize generate code
        if (System.getProperty("idea.sync.active") != null) {
            kotlin.srcDir("$buildDir/generated/ksp/jvmMain/kotlin")
        }
    }
}
```


## FAQ

**What does *kinzhal* mean?**

It's a [russian word](https://en.wiktionary.org/wiki/кинжал) for dagger. Yes, *K* is not a pun
  
**Will this project become deprecated after Dagger releases [support for KSP and Multiplatform](https://github.com/google/dagger/issues/2349)?** 

Probably. But we'll see

## Examples

```kotlin
@AppScope
@Component(modules = [
    NetworkModule::class,
], dependencies = [
    AppDependencies::class,
])
interface AppComponent {
    fun createAuthPresenter(): AuthPresenter
}

interface AppDependencies {
    val application: Application
}

@Scope
annotation class AppScope

class Application

@AppScope
class Database @Inject constructor(application: Application)

class AuthPresenter @Inject constructor(database: Database, lastFmApi: LastFmApi)

class HttpClient

interface LastFmApi

class LastFmKtorApi @Inject constructor(client: HttpClient) : LastFmApi

interface NetworkModule {
    companion object {
        @AppScope
        fun provideHttpClient() = HttpClient()
    }

    fun bindLastFm(lastFmApi: LastFmKtorApi): LastFmApi
}

// somewhere in your app
val component = KinzhalAppComponent(object : AppDependencies {
    override val application = Application()
})

val presenter = component.createAuthPresenter()
```
See more in the [source code](https://github.com/daugeldauge/kinzhal/tree/master/sample/src/commonMain/kotlin/com/daugeldauge/kinzhal/sample/graph)
  
## Dagger2 compatibility table

| Feature    | Kinzhal support | Notes      |
| ---------- | --------------- | -----------|
| `@Component` | ✅ | |
| Constructor injection | ✅ | |
| Field injection | 🚫 | |
| Component provision functions and properties | ✅ | |
| `@Module` | ⚠️ | Kinzhal has modules but does not have `@Module` annotation. All classes in component module list are treated as modules. Only `object` modules with provides=functions and `interface` modules with binds-functions are allowed |
| `@Provides` | ⚠️ | Kinzhal does not have `@Provides` annotation. All non-abstract functions in a module are considered to be provides-functions |
| `@Binds` | ⚠️ | Kinzhal does not have `@Binds` annotation. All abstract functions in a module are considered to be binds-functions |
| `@Scope` | ✅ | |
| `@Qualifier` | ✅ | |
| Component dependencies | ✅ | Dependency instances are passed to generated component's constructor instead of builder functions |
| `@Subcomponent` | 🚫 | You can use component dependency to emulate behaviour of subcomponents |
| `@Reusable` | 🚫 | |
| `@BindsInstance` | 🚫 | You can use component dependency to bind instances |
| Lazy/provider injections | 🚫 | |
| `@BindsOptionalOf` | 🚫 |
| Multibindings | 🚫 | |
| Assisted injection | 🚫 | |


