plugins {
    id("com.github.johnrengelman.shadow") version "6.1.0"
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("io.grpc:grpc-kotlin-stub:1.0.0")
    implementation("io.grpc:grpc-netty-shaded:1.34.1")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.1")
    implementation("com.squareup.sqldelight:sqlite-driver:1.4.4")
}

sqldelight {
    database("Database") {
        packageName = "plp.data"
    }
}

val main = "plp.ear.MainKt"

application {
    mainClassName = main // needed for shadow plugin
    mainClass.set(main)
}

val run by tasks.getting(JavaExec::class) {
    standardInput = System.`in`
}
