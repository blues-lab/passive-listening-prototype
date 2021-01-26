package plp.common.rpc.server

import io.grpc.ServerBuilder
import io.grpc.kotlin.AbstractCoroutineServerImpl
import plp.logging.KotlinLogging
import java.io.File
private val logger = KotlinLogging.logger {}

class TlsGrpcServer(
    service: AbstractCoroutineServerImpl,
    private val port: Int,
    certChainFilePath: String,
    privateKeyFilePath: String,
) : GrpcServer(

    ServerBuilder.forPort(port).useTransportSecurity(File(certChainFilePath), File(privateKeyFilePath))
        .addService(service).build().also {
            logger.debug { "using server with TLS on port $port" }
        }
)
