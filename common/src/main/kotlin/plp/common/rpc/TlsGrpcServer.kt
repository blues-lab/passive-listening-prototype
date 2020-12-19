package plp.common.rpc

import io.grpc.ServerBuilder
import io.grpc.kotlin.AbstractCoroutineServerImpl
import mu.KotlinLogging
import java.io.File
private val logger = KotlinLogging.logger {}

class TlsGrpcServer(
    service: AbstractCoroutineServerImpl,
    private val port: Int = DEFAULT_PORT,
    certChainFilePath: String,
    privateKeyFilePath: String,
) : GrpcServer(

    ServerBuilder.forPort(port).useTransportSecurity(File(certChainFilePath), File(privateKeyFilePath))
        .addService(service).build().also {
            logger.debug { "using server with TLS on port $port" }
        }
)
