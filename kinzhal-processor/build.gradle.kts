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
                implementation(projects.kinzhalAnnotations)
                implementation(libs.ksp.symbolProcessingApi)
                implementation(libs.kotlinpoet)
            }
        }

        getByName("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.compileTestingKsp)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.allWarningsAsErrors = true
}
