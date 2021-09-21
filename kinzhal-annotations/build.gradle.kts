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
    macosArm64()
    macosX64()
    watchos()
    tvos()
    linuxX64()
}
