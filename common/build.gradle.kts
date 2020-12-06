plugins {
    id("plp.conventions")
}

dependencies {
    implementation("io.grpc:grpc-kotlin-stub:0.2.1")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
}
