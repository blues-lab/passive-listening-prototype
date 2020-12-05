import io.grpc.Server
import mu.KotlinLogging

const val DEFAULT_PORT = 50057

private val logger = KotlinLogging.logger {}
abstract class GrpcServer(
    val server: Server
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
}
