plugins {
    kotlin("jvm") version "1.5.30" apply false
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.5.30"))
    }
}

repositories {
    mavenCentral()
}

