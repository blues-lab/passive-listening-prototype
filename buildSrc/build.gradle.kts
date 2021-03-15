plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.4.31"
}

repositories {
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.31")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:10.0.0")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.16.0")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.38.0")
    implementation("gradle.plugin.com.google.protobuf:protobuf-gradle-plugin:0.8.15")
}
