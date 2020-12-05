import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.20"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("io.gitlab.arturbosch.detekt").version("1.15.0-RC1")
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
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("io.grpc:grpc-kotlin-stub:0.2.1")
    implementation("io.grpc:grpc-netty-shaded:1.34.0")
//    implementation("io.grpc:grpc-protobuf:1.34.0")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
//    implementation("com.google.protobuf:protobuf-java-util:3.14.0")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.3")
    implementation("org.slf4j:slf4j-jdk14:1.7.30")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.1")

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
