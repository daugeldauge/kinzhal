plugins {
    kotlin("multiplatform")
    `publish-conventions`
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    ios()
    iosSimulatorArm64()
    macosArm64()
    macosX64()
    watchos()
    watchosSimulatorArm64()
    tvos()
    tvosSimulatorArm64()
    linuxX64()
}
