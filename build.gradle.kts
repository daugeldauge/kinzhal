@Suppress("DSL_SCOPE_VIOLATION") // https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    kotlin("jvm") version libs.versions.kotlin.get() apply false
}

repositories {
    mavenCentral()
}
