package plp.hub

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import plp.common.RECORDING_SERVICE_PORT
import plp.common.configureLogging
import plp.common.resolveHomeDirectory
import plp.common.rpc.server.MutualTlsGrpcServer
import plp.logging.KotlinLogging
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

@ExperimentalPathApi
fun main(args: Array<String>) {
    configureLogging()

    val parser = ArgParser("RecordingServer")
    val key by parser.option(ArgType.String).required()
    val cert by parser.option(ArgType.String).required()
    val root by parser.option(ArgType.String).required()
    val tmpDir by parser.option(ArgType.String).required()
    parser.parse(args)

    val logger = KotlinLogging.logger {}
    logger.info("starting Recording server")
    println("starting Recording server!")

    val service = RecordingService(Path(resolveHomeDirectory(tmpDir)))
    val server = MutualTlsGrpcServer(service, RECORDING_SERVICE_PORT, cert, key, root)
    server.start()
    server.blockUntilShutdown()
}
