plugins {
    id("com.google.devtools.ksp") version "1.5.30-1.0.0"
    kotlin("jvm")
    kotlin("kapt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":kinzhal-processor"))
    ksp(project(":kinzhal-processor"))

    implementation("com.google.dagger:dagger:2.38.1")
    kapt("com.google.dagger:dagger-compiler:2.38.1") {
        exclude(group = "com.google.devtools.ksp")
    }
}
