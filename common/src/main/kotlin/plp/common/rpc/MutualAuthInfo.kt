package plp.common.rpc

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext
import plp.common.toPath
import kotlin.io.path.ExperimentalPathApi

@ExperimentalPathApi
class MutualAuthInfo(root: String, cert: String, key: String) {
    val sslContext: SslContext = GrpcSslContexts.forClient()
        .trustManager(root.toPath().toFile())
        .keyManager(cert.toPath().toFile(), key.toPath().toFile())
        .build()
}
