plugins {
    kotlin("multiplatform")
    `publish-conventions`
}

repositories {
    mavenCentral()
//    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

kotlin {
    sourceSets {
        jvm()

        getByName("jvmMain") {
            dependencies {
                implementation(project(":kinzhal-annotations"))
                implementation("com.google.devtools.ksp:symbol-processing-api:1.5.30-1.0.0")
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
