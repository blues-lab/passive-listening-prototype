package plp.common.rpc.server

import io.grpc.Server
import io.grpc.kotlin.AbstractCoroutineServerImpl
import plp.logging.KotlinLogging

private val logger = KotlinLogging.logger {}

open class GrpcServer(
    private val server: Server
) {
    fun start() {
        logger.debug { "getting ready to start" }
        server.start()
        logger.debug { "server started" }
        Runtime.getRuntime().addShutdownHook(
            Thread {
                logger.debug { "shutting down gRPC server since JVM is shutting down" }
                this@GrpcServer.stop()
                logger.debug { "server shut down" }
            }
        )
    }

    fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    companion object {
        @Suppress("ReturnCount")
        fun fromArgs(
            service: AbstractCoroutineServerImpl,
            port: Int,
            certChainFilePath: String?,
            privateKeyFilePath: String?,
            trustCertCollectionFilePath: String?,
        ): GrpcServer {
            if ((certChainFilePath != null) && (privateKeyFilePath != null)) {
                if ((trustCertCollectionFilePath != null)) {
                    logger.debug { "for RPC, using mutual TLS with parameters root = $trustCertCollectionFilePath, cert = $certChainFilePath, key = $privateKeyFilePath" }
                    return MutualTlsGrpcServer(
                        service = service,
                        port = port,
                        certChainFilePath = certChainFilePath,
                        trustCertCollectionFilePath = trustCertCollectionFilePath,
                        privateKeyFilePath = privateKeyFilePath
                    )
                }

                logger.debug { "for RPC, using TLS with parameters root = $trustCertCollectionFilePath" }
                return TlsGrpcServer(
                    service,
                    port,
                    certChainFilePath = certChainFilePath,
                    privateKeyFilePath = privateKeyFilePath
                )
            }

            logger.warning("for RPC, using no authentication (not secure over the network!)")
            return InsecureGrpcServer(service, port)
        }
    }
}
