plugins {
    id("com.google.devtools.ksp") version "1.6.21-1.0.6"
    kotlin("multiplatform")
}

repositories {
    if (hasProperty("useSnapshotForSample")) {
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
    mavenCentral()
}

dependencies {
    ksp(kinzhalDependency(projects.kinzhalProcessor))
}

kotlin {
    sourceSets {
        jvm()
        ios()
        macosX64()

        getByName("commonMain") {
            dependencies {
                implementation(kinzhalDependency(projects.kinzhalAnnotations))
            }

            if (System.getProperty("idea.sync.active") != null) {
                kotlin.srcDir("$buildDir/generated/ksp/jvm/jvmMain/kotlin") // only needed for IDE to see generated code // TODO figure out how to avoid this
            }
        }

        getByName("commonTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }

    }
}

fun kinzhalDependency(project: ProjectDependency): Any {
    return if (!hasProperty("useSnapshotForSample")) {
        project
    } else {
        "${project.group}:${project.name}:$version-SNAPSHOT"
    }
}
