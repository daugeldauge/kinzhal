enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    versionCatalogs {
        create("libs") {
            val kotlinVersion = "1.6.21"
            version("kotlin", kotlinVersion)

            val kspVersion = "$kotlinVersion-1.0.6"

            library("kotlinpoet", "com.squareup:kotlinpoet:1.9.0")
            library("compileTestingKsp", "com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.4")

            library("ksp-SymbolProcessingApi", "com.google.devtools.ksp", "symbol-processing-api").version(kspVersion)
            plugin("ksp", "com.google.devtools.ksp").version(kspVersion)
        }
    }
}

rootProject.name = "kinzhal"

include(":kinzhal-processor")
include(":kinzhal-annotations")
include(":sample")
