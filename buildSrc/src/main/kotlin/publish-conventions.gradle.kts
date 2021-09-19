import org.gradle.jvm.tasks.Jar

plugins {
    `maven-publish`
    signing
}

group = "com.daugeldauge.kinzhal"
version = "0.0.1"

if (!release) {
    version = "$version-SNAPSHOT"
}

publishing {
    publications.withType<MavenPublication> {

        // Empty javadoc
        artifact(tasks.findByName("javadocJar") ?: task<Jar>("javadocJar") {
            archiveClassifier.set("javadoc")
        })

        pom {
            name.set("${project.group}:${project.name}")
            description.set("Compile-time dependency injection for Kotlin Multiplatform")
            url.set("https://github.com/daugeldauge/kinzhal")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    name.set("Artem Daugel-Dauge")
                    email.set("artem@daugeldauge.com")
                    url.set("http://daugeldauge.com")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/daugeldauge/kinzhal.git")
                developerConnection.set("scm:git:git://github.com/daugeldauge/kinzhal.git")
                url.set("https://github.com/daugeldauge/kinzhal")
            }
        }
    }

    signing {
        sign(publications)
    }

    repositories {
        maven {
            url = when {
                release -> "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                else -> "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            }.let(::uri)

            credentials {
                username = stringProperty("sonatype.username")
                password = stringProperty("sonatype.password")
            }
        }
    }
}

val release: Boolean
    get() = hasProperty("release")

fun stringProperty(propertyName: String) = findProperty(propertyName) as String?
