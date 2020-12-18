plugins {
    id("plp.conventions")
}

dependencies {
    api("io.grpc:grpc-kotlin-stub:1.0.0")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
}
