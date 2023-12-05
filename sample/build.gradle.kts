import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon

plugins {
    alias(libs.plugins.ksp)
    kotlin("multiplatform")
}

repositories {
    if (hasProperty("useSnapshotForSample")) {
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
    mavenCentral()
}

kotlin {
    sourceSets {
        jvm()
        iosX64()
        iosArm64()
        iosSimulatorArm64()
        macosX64()

        commonMain {
            dependencies {
                implementation(kinzhalDependency(projects.kinzhalAnnotations))
            }

            kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin")) // only needed for IDE to see generated code // TODO figure out how to avoid this
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }

    }
}

dependencies  {
    kspCommonMainMetadata(kinzhalDependency(projects.kinzhalProcessor))
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().configureEach {
    if (this !is KotlinCompileCommon) {
        dependsOn("kspCommonMainKotlinMetadata")
    }

    kotlinOptions.allWarningsAsErrors = true
}


fun kinzhalDependency(project: ProjectDependency): Any {
    return if (!hasProperty("useSnapshotForSample")) {
        project
    } else {
        "${project.group}:${project.name}:$version-SNAPSHOT"
    }
}
