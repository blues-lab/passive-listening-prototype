plugins {
    java
    application
    id("plp.conventions")
}

dependencies {
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("io.grpc:grpc-kotlin-stub:1.0.0")
    implementation("io.grpc:grpc-netty-shaded:1.34.1")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.1")
}

application {
    mainClass.set("plp.ear.StandaloneContinuousRecorderKt")
}
