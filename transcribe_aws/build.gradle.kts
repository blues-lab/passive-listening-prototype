import com.google.protobuf.gradle.protobuf

plugins {
    application
    id("plp.conventions")
    id("plp.grpc")
    kotlin("plugin.serialization") version "1.5.0"
}

val awsSdkVersion = "2.16.67"

dependencies {
    implementation(project(":common"))
    implementation(project(":logging"))
    api(project(":proto")) // only necessary for IntelliJ to find sources
    protobuf(project(":proto"))
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.2")
    implementation("software.amazon.awssdk:s3:$awsSdkVersion")
    implementation("software.amazon.awssdk:transcribe:$awsSdkVersion")
    runtimeOnly("org.slf4j:slf4j-simple:1.7.30")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
}

application {
    mainClass.set("plp.transcribe.aws.TranscriptionServiceMainKt")
}
