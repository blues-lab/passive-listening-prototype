package plp.brain

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import plp.common.configureLogging
import plp.common.resolveHomeDirectory
import plp.common.rpc.DEFAULT_PORT
import plp.common.rpc.MutualTlsGrpcServer
import plp.logging.KotlinLogging
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

@ExperimentalPathApi
fun main(args: Array<String>) {
    configureLogging()

    val parser = ArgParser("TranscriptionServer")
    val key by parser.option(ArgType.String).required()
    val cert by parser.option(ArgType.String).required()
    val root by parser.option(ArgType.String).required()
    val model by parser.option(ArgType.String).required()
    val tmpDir by parser.option(ArgType.String).required()
    parser.parse(args)

    val logger = KotlinLogging.logger {}
    logger.info("starting Transcription server")

    val service = TranscriptionService(Path(resolveHomeDirectory(model)), Path(resolveHomeDirectory(tmpDir)))
    val server = MutualTlsGrpcServer(service, DEFAULT_PORT, cert, key, root)
    server.start()
    server.blockUntilShutdown()
}
