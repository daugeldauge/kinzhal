plugins {
    id("com.google.devtools.ksp") version "1.5.30-1.0.0"
    kotlin("jvm")
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":kinzhal-processor"))
    ksp(project(":kinzhal-processor"))
}
