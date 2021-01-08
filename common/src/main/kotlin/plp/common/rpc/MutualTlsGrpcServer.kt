package plp.common.rpc

import io.grpc.kotlin.AbstractCoroutineServerImpl
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder
import plp.logging.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * via https://github.com/grpc/grpc-java/blob/master/examples/example-tls/src/main/java/io/grpc/examples/helloworldtls/HelloWorldServerTls.java
 */
internal fun makeSslContext(
    certChainFilePath: String,
    privateKeyFilePath: String,
    trustCertCollectionFilePath: String,
): SslContext {
    val sslClientContextBuilder: SslContextBuilder = SslContextBuilder.forServer(
        File(certChainFilePath),
        File(privateKeyFilePath)
    )
    sslClientContextBuilder.trustManager(File(trustCertCollectionFilePath))
    sslClientContextBuilder.clientAuth(ClientAuth.REQUIRE)
    return GrpcSslContexts.configure(sslClientContextBuilder).build()
}

class MutualTlsGrpcServer(
    service: AbstractCoroutineServerImpl,
    port: Int = DEFAULT_PORT,
    certChainFilePath: String,
    privateKeyFilePath: String,
    trustCertCollectionFilePath: String,
) : GrpcServer(
    NettyServerBuilder.forPort(port)
        .addService(service)
        .sslContext(makeSslContext(certChainFilePath, privateKeyFilePath, trustCertCollectionFilePath))
        .build()
        .also {
            logger.debug { "using server with mutual TLS on port $port" }
        }
)
