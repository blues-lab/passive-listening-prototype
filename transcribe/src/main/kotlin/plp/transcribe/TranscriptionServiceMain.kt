package plp.transcribe

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import plp.common.CONFIG_FILENAME
import plp.common.GLOBAL_CONFIG
import plp.common.Transcriber
import plp.common.configureLogging
import plp.common.resolveHomeDirectory
import plp.common.rpc.client.GrpcChannelChoice
import plp.common.rpc.server.GrpcServer
import plp.hub.vad.ChromeTranscriber
import plp.logging.KotlinLogging
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

@ExperimentalPathApi
fun main(args: Array<String>) {
    configureLogging()

    val parser = ArgParser("TranscriptionServer")
    val model by parser.option(
        ArgType.String,
        description = "the directory containing the wav2letter model. If omitted, server will respond with fake transcriptions instead of actually invoking the transcription process"
    )
    val chrome by parser.option(
        ArgType.Boolean,
        description = "whether to enable chrome transcription"
    )
    val tmpDir by parser.option(ArgType.String, description = "path for temporarily storing files being transcribed").required()
    val root by parser.option(ArgType.String, description = "path to root certificate chain, if using mutual TLS")
    val key by parser.option(ArgType.String, description = "path to secret key file, if using TLS")
    val cert by parser.option(ArgType.String, description = "path to public certificate, if using TLS")
    val config by parser.option(ArgType.String, description = "path to config file (overrides default)")
    val parallel by parser.option(
        ArgType.Int,
        shortName = "p",
        description = "maximum number of Docker containers to launch in parallel"
    ).default(1)
    parser.parse(args)

    if (config != null) {
        CONFIG_FILENAME = config!!
    }

    val logger = KotlinLogging.logger {}
    logger.info("starting Transcription server")

    val transcriber: Transcriber = if (model == null) {
        if (chrome == null) {
            logger.error("missing argument: `model` (wav2letter model path). Server will respond with fake transcriptions instead of actually invoking the transcription process")
            FakeTranscriber()
        } else {
            val channelChoice: GrpcChannelChoice = GrpcChannelChoice.fromArgs(root = root, cert = cert, key = key)
            ChromeTranscriber(channelChoice)
        }
    } else {
        Wav2letterTranscriber(
            Path(resolveHomeDirectory(model!!)),
            parallel
        )
    }
    val service = TranscriptionService(transcriber, Path(resolveHomeDirectory(tmpDir)))

    val server = GrpcServer.fromArgs(
        service,
        GLOBAL_CONFIG.transcriptionService.port,
        certChainFilePath = cert,
        privateKeyFilePath = key,
        trustCertCollectionFilePath = root
    )
    server.start()
    server.blockUntilShutdown()
}
