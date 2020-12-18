plugins {
    java
    application
    id("plp.conventions")
    id("plp.grpc")
}

dependencies {
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.1")
}

application {
    mainClass.set("plp.brain.TranscriptionServerKt")
}
