plugins {
    application
    id("plp.grpc")
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
}

/* Explicitly telling Gradle about the generated sources is only necessary for IntelliJ to learn about them
 * because of bug https://youtrack.jetbrains.com/issue/IDEA-209418
 * see also https://stackoverflow.com/q/65219519
 * Otherwise, the Protobuf plugin picks the generated classes up without any additional configuration.
 */
sourceSets {
    main {
        java {
            srcDirs(
                "build/generated/source/proto/main/java",
                "build/generated/source/proto/main/grpc",
                "build/generated/source/proto/main/grpckt"
            )
        }
    }
}
