import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id("plp.conventions")
    id("com.google.protobuf")
}

val grpcVersion = "1.34.1"
val grpcKotlinVersion = "1.0.0"
val protobufVersion = "3.14.0"

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

dependencies {
    api("com.google.protobuf:protobuf-java-util:$protobufVersion")
    api("io.grpc:grpc-kotlin-stub:1.0.0")
    implementation("io.grpc:grpc-netty-shaded:1.34.1")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk7@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}
