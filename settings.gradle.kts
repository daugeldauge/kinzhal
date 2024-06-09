enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val kotlinVersion = "1.9.24"
            version("kotlin", kotlinVersion)

            val kspVersion = "$kotlinVersion-1.0.20"
            val kotlinpoet = "1.13.1"

            library("kotlinpoet", "com.squareup", "kotlinpoet").version(kotlinpoet)
            library("kotlinpoet-ksp", "com.squareup", "kotlinpoet-ksp").version(kotlinpoet)
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
