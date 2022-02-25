plugins {
    kotlin("multiplatform")
    `publish-conventions`
}

repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        jvm()

        getByName("jvmMain") {
            dependencies {
                implementation(project(":kinzhal-annotations"))
                implementation("com.google.devtools.ksp:symbol-processing-api:1.6.10-1.0.4")
                implementation("com.squareup:kotlinpoet:1.9.0")
            }
        }

        getByName("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.4")
            }
        }
    }
}
