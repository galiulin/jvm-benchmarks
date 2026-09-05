// This root build file is deliberately thin: each benchmark is an independent
// Gradle module with its own build.gradle.kts (see pointer-chasing/build.gradle.kts).
// Only what's actually shared lives here: plugin versions and repositories.
plugins {
    kotlin("jvm") version "2.4.10" apply false
    id("me.champeau.jmh") version "0.7.3" apply false
}

allprojects {
    group = "io.github.galiulin.benchmarks"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }
}
