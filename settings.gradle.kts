enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "kinzhal"

include(":kinzhal-processor")
include(":kinzhal-annotations")
include(":sample")
