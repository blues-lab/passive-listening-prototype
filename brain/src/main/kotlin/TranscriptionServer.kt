import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import mu.KotlinLogging
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

fun configureLogging() {
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace")

    val path: String = {}.javaClass.getResource("logging.properties").file
    val loggingConfig = System.getProperty("java.util.logging.config.file", path)
    System.setProperty("java.util.logging.config.file", loggingConfig)
}

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

    val port = 50057
    val service = TranscriptionService(Path(resolveHomeDirectory(model)), Path(resolveHomeDirectory(tmpDir)))
    val server = MutualTlsGrpcServer(service, port, cert, key, root)
    server.start()
    server.blockUntilShutdown()
}
