import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
}

group = "edu.berkeley.cs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://kotlin.bintray.com/kotlinx")
    }
}


dependencies {
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.3")
    implementation("org.slf4j:slf4j-jdk14:1.7.30")

    // JUnit
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "15"
}

tasks.test {
    useJUnitPlatform()
}

tasks.getByPath("detekt").onlyIf { gradle.startParameter.taskNames.contains("detekt") }
