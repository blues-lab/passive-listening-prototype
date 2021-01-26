package plp.common.rpc.server

import io.grpc.ServerBuilder
import io.grpc.kotlin.AbstractCoroutineServerImpl
import plp.logging.KotlinLogging

private val logger = KotlinLogging.logger {}

open class InsecureGrpcServer(
    service: AbstractCoroutineServerImpl,
    private val port: Int,
) : GrpcServer(
    ServerBuilder.forPort(port).addService(service).build().also {
        logger.debug { "using open (no TLS) server on port $port" }
    }
)
