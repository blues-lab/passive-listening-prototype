plugins {
    java
    application
    id("plp.conventions")
}

dependencies {
    implementation(project(":common"))
    implementation("io.grpc:grpc-kotlin-stub:0.2.1")
    implementation("io.grpc:grpc-netty-shaded:1.34.0")
//    implementation("io.grpc:grpc-protobuf:1.34.0")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.1")
}

application {
    mainClass.set("plp.brain.TranscriptionServerKt")
}
