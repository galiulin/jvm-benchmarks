import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    // The benchmarks live in src/jmh/java on purpose, not src/jmh/kotlin: that way
    // JMH's annotations are processed by plain javac APT, without kapt.
    id("me.champeau.jmh")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
}

dependencies {
    val kotestVersion = "6.2.4"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Iteration and fork counts are set via @Warmup/@Measurement/@Fork annotations right
// on the benchmark class (see src/jmh/java), so they stay visible next to the code
// they measure. Flags passed here through jmh{} reach JMH as CLI options and would
// override the annotations, so this block only configures the result format.
jmh {
    jmhVersion.set("1.37")
    resultFormat.set("JSON")
    resultsFile.set(layout.buildDirectory.file("results/jmh/results.json"))
}
