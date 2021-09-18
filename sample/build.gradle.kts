plugins {
    id("com.google.devtools.ksp") version "1.5.30-1.0.0"
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

dependencies {
    ksp(project(":kinzhal-processor"))
}


kotlin {
    sourceSets {
        jvm()
        ios()

        getByName("commonMain") {
            dependencies {
                implementation(project(":kinzhal-annotations"))
                kotlin.srcDir("$buildDir/generated/ksp/jvmMain/kotlin") // only needed for IDE to see generated code // TODO figure out how to avoid this
            }
        }

        getByName("commonTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }

    }

}
