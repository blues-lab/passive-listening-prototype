package plp.transcribe.aws

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import plp.common.CONFIG_FILENAME
import plp.common.GLOBAL_CONFIG
import plp.common.resolveHomeDirectory
import plp.common.rpc.server.GrpcServer
import plp.logging.KotlinLogging
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

@ExperimentalPathApi
fun main(args: Array<String>) {
    val parser = ArgParser("TranscriptionServer")
    val tmpDir by parser.option(ArgType.String, description = "path for temporarily storing files being transcribed")
        .required()
    val root by parser.option(ArgType.String, description = "path to root certificate chain, if using mutual TLS")
    val key by parser.option(ArgType.String, description = "path to secret key file, if using TLS")
    val cert by parser.option(ArgType.String, description = "path to public certificate, if using TLS")
    val config by parser.option(ArgType.String, description = "path to config file (overrides default)")
    parser.parse(args)

    if (config != null) {
        CONFIG_FILENAME = config!!
    }

    val logger = KotlinLogging.logger {}
    logger.info("starting Transcription server")

    val s3 = S3()
    val transcriber = AwsTranscribe(s3)
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
