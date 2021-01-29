plugins {
    id("plp.conventions")
    id("plp.grpc")
    kotlin("plugin.serialization") version "1.4.21"
}

dependencies {
    implementation(project(":logging"))
    api("io.grpc:grpc-kotlin-stub:1.0.0")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    implementation("io.ktor:ktor-serialization:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation("org.hjson:hjson:3.0.0")
}
