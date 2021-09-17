plugins {
    id("com.google.devtools.ksp") version "1.5.30-1.0.0"
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(project(":kinzhal-annotations"))
                implementation(kotlin("test"))
                configurations["ksp"].dependencies.add(project(":kinzhal-processor"))
            }
        }
    }

    jvm()
    ios()
}
