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
}
