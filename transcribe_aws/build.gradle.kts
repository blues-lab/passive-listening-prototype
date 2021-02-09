import com.google.protobuf.gradle.protobuf

plugins {
    application
    id("plp.conventions")
    id("plp.grpc")
}

val awsSdkVersion = "2.15.78"

dependencies {
    implementation(project(":common"))
    implementation(project(":logging"))
    api(project(":proto")) // only necessary for IntelliJ to find sources
    protobuf(project(":proto"))
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.1")
    implementation("software.amazon.awssdk:s3:$awsSdkVersion")
    implementation("software.amazon.awssdk:transcribe:$awsSdkVersion")
    runtimeOnly("org.slf4j:slf4j-simple:1.7.30")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
}

application {
    mainClass.set("plp.transcribe.aws.TranscriptionServiceMainKt")
}
