package plp.brain

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import plp.common.TRANSCRIPTION_SERVICE_PORT
import plp.common.configureLogging
import plp.common.resolveHomeDirectory
import plp.common.rpc.server.GrpcServer
import plp.logging.KotlinLogging
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

@ExperimentalPathApi
fun main(args: Array<String>) {
    configureLogging()

    val parser = ArgParser("TranscriptionServer")
    val model by parser.option(ArgType.String).required()
    val tmpDir by parser.option(ArgType.String).required()
    val fake by parser.option(
        ArgType.Boolean,
        description = "respond with fake transcriptions instead of actually invoking the transcription process"
    ).default(false)
    val root by parser.option(ArgType.String, description = "path to root certificate chain, if using mutual TLS")
    val key by parser.option(ArgType.String, description = "path to secret key file, if using TLS")
    val cert by parser.option(ArgType.String, description = "path to public certificate, if using TLS")
    parser.parse(args)

    val logger = KotlinLogging.logger {}
    logger.info("starting Transcription server")

    val service = if (fake) FakeTranscriptionService() else TranscriptionService(
        Path(resolveHomeDirectory(model)),
        Path(resolveHomeDirectory(tmpDir))
    )
    val server = GrpcServer.fromArgs(
        service,
        TRANSCRIPTION_SERVICE_PORT,
        certChainFilePath = cert,
        privateKeyFilePath = key,
        trustCertCollectionFilePath = root
    )
    server.start()
    server.blockUntilShutdown()
}
