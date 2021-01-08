import com.google.protobuf.gradle.protobuf

plugins {
    java
    application
    id("plp.conventions")
    id("plp.grpc")
    id("com.squareup.sqldelight") version "1.4.4"
}

dependencies {
    implementation(project(":common"))
    implementation(project(":logging"))
    api(project(":proto")) // only necessary for IntelliJ to find sources
    protobuf(project(":proto"))
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    implementation("io.grpc:grpc-kotlin-stub:1.0.0")
    implementation("io.grpc:grpc-netty-shaded:1.34.1")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.1")
    implementation("com.squareup.sqldelight:sqlite-driver:1.4.4")
    // runtimeOnly("org.xerial:sqlite-jdbc:3.34.0")
}

application {
    mainClass.set("plp.brain.RecordingClientMainKt")
}

sqldelight {
    database("Database") {
        packageName = "plp.data"
    }
}
