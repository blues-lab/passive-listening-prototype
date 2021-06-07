plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    java
    application
    id("plp.conventions")
    id("plp.grpc")
    id("com.squareup.sqldelight") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.10"
}

val ktorVersion = "1.6.0"

dependencies {
    implementation(project(":common"))
    implementation(project(":logging"))
    api(project(":proto")) // only necessary for IntelliJ to find sources
    protobuf(project(":proto"))
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.2")
    implementation("com.squareup.sqldelight:sqlite-driver:1.5.0")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-mustache:$ktorVersion")
    runtimeOnly("org.slf4j:slf4j-simple:1.7.30")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
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
