plugins {
    kotlin("multiplatform")
    `publish-conventions`
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(projects.kinzhalAnnotations)
            implementation(libs.ksp.symbolProcessingApi)
            implementation(libs.kotlinpoet)
            implementation(libs.kotlinpoet.ksp)
        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compileTestingKsp)
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>> {
    compilerOptions {
        allWarningsAsErrors = true
    }
}
