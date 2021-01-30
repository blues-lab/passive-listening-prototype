plugins {
    id("com.github.johnrengelman.shadow") version "6.1.0"
    java
    application
    id("plp.conventions")
    id("plp.grpc")
    id("com.squareup.sqldelight") version "1.4.4"
    kotlin("plugin.serialization") version "1.4.21"
}

dependencies {
    implementation(project(":common"))
    implementation(project(":logging"))
    api(project(":proto")) // only necessary for IntelliJ to find sources
    protobuf(project(":proto"))
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.1")
    implementation("com.squareup.sqldelight:sqlite-driver:1.4.4")
    implementation("io.ktor:ktor-server-netty:1.5.1")
    implementation("io.ktor:ktor-serialization:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
}

repositories {
    jcenter()
}

sqldelight {
    database("Database") {
        packageName = "plp.data"
    }
}

val main = "plp.hub.HubMainKt"

application {
    mainClassName = main // needed for shadow plugin
    mainClass.set(main)
}

val run by tasks.getting(JavaExec::class) {
    standardInput = System.`in`
}
