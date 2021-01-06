package plp.ear

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import mu.KotlinLogging
import plp.common.configureLogging
import plp.common.resolveHomeDirectory
import plp.common.rpc.MutualTlsGrpcServer
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

const val DEFAULT_PORT = 50058

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
    val server = MutualTlsGrpcServer(service, DEFAULT_PORT, cert, key, root)
    server.start()
    server.blockUntilShutdown()
}
