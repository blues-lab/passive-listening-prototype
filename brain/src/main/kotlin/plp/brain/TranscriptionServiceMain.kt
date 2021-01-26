package plp.brain

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import plp.common.TRANSCRIPTION_SERVICE_PORT
import plp.common.configureLogging
import plp.common.resolveHomeDirectory
import plp.common.rpc.server.MutualTlsGrpcServer
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
    val fake by parser.option(
        ArgType.Boolean,
        description = "respond with fake transcriptions instead of actually invoking the transcription process"
    ).default(false)
    parser.parse(args)

    val logger = KotlinLogging.logger {}
    logger.info("starting Transcription server")

    val service = if (fake) FakeTranscriptionService() else TranscriptionService(
        Path(resolveHomeDirectory(model)),
        Path(resolveHomeDirectory(tmpDir))
    )
    val server = MutualTlsGrpcServer(service, TRANSCRIPTION_SERVICE_PORT, cert, key, root)
    server.start()
    server.blockUntilShutdown()
}
