# DEPRECATED

Kinzhal is deprecated in favor of [kotlin-inject](https://github.com/evant/kotlin-inject)

# Kinzhal

Kinzhal is a Kotlin Multiplatform library for compile-time dependency injection. The goal is to emulate basic features of [Dagger](https://dagger.dev/) to achieve similar experience in Kotlin Multiplatform projects

Kinzhal is based on [Kotlin Symbol Processing](https://github.com/google/ksp) (KSP) — the API for lightweight compiler plugins. You'll need to set up KSP in your project to use Kinzhal

## Setup

Add KSP to your plugins section:
```kotlin
plugins {
    id("com.google.devtools.ksp") version "$kspVersion"
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

**What targets are supported?**

JVM and most of Kotlin/Native targets. Kotlin/JS is not supported for now (see [#4](https://github.com/daugeldauge/kinzhal/issues/4)). Please file an issue if you don't find a target you need

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
| Field injection | 🚫 | [#1](https://github.com/daugeldauge/kinzhal/issues/1) |
| Component provision functions and properties | ✅ | |
| `@Module` | ⚠️ | Kinzhal has modules but does not have `@Module` annotation. All classes in component module list are treated as modules. Only `object` modules with provides=functions and `interface` modules with binds-functions are allowed |
| `@Provides` | ⚠️ | Kinzhal does not have `@Provides` annotation. All non-abstract functions in a module are considered to be provides-functions |
| `@Binds` | ⚠️ | Kinzhal does not have `@Binds` annotation. All abstract functions in a module are considered to be binds-functions |
| `@Scope` | ✅ | |
| `@Qualifier` | ✅ | |
| Component dependencies | ✅ | Dependency instances are passed to generated component's constructor instead of builder functions |
| `@Subcomponent` | 🚫 | [#3](https://github.com/daugeldauge/kinzhal/issues/3) <br> You can use component dependency to emulate behaviour of subcomponents |
| `@Reusable` | 🚫 | |
| `@BindsInstance` | 🚫 | You can use component dependency to bind instances |
| Lazy/provider injections | 🚫 | [#2](https://github.com/daugeldauge/kinzhal/issues/2) |
| `@BindsOptionalOf` | 🚫 |
| Multibindings | 🚫 | |
| Assisted injection | ✅ | |
