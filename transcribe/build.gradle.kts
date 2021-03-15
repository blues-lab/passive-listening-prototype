import com.google.protobuf.gradle.protobuf

plugins {
    // java
    application
    id("plp.conventions")
    id("plp.grpc")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":logging"))
    api(project(":proto")) // only necessary for IntelliJ to find sources
    protobuf(project(":proto"))
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.2")
}

application {
    mainClass.set("plp.transcribe.TranscriptionServiceMainKt")
}
