plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.5.10"
}

repositories {
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:10.1.0")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.17.1")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.39.0")
    implementation("gradle.plugin.com.google.protobuf:protobuf-gradle-plugin:0.8.16")
}
