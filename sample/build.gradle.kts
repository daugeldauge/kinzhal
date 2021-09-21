plugins {
    id("com.google.devtools.ksp") version "1.5.30-1.0.0"
    kotlin("multiplatform")
}

repositories {
//    mavenLocal()
//    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
    mavenCentral()
}

dependencies {
    ksp(project(":kinzhal-processor"))
//    ksp("com.daugeldauge.kinzhal:kinzhal-processor:0.0.5-SNAPSHOT")
}


kotlin {
    sourceSets {
        jvm()
        ios()
        macosX64()

        getByName("commonMain") {
            dependencies {
                implementation(project(":kinzhal-annotations"))
//                implementation("com.daugeldauge.kinzhal:kinzhal-annotations:0.0.5-SNAPSHOT")
            }

            if (System.getProperty("idea.sync.active") != null) {
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
