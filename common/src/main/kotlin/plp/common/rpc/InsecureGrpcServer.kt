package plp.common.rpc

import io.grpc.ServerBuilder
import io.grpc.kotlin.AbstractCoroutineServerImpl
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

open class InsecureGrpcServer(
    service: AbstractCoroutineServerImpl,
    private val port: Int = DEFAULT_PORT,
) : GrpcServer(
    ServerBuilder.forPort(port).addService(service).build().also {
        logger.debug { "using open (no TLS) server on port $port" }
    }
)
