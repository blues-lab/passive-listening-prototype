package plp.hub

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.required
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import plp.common.CONFIG_FILENAME
import plp.common.configureLogging
import plp.common.rpc.client.GrpcChannelChoice
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

@ExperimentalCli
@ExperimentalCoroutinesApi
@ExperimentalPathApi
fun main(args: Array<String>) {
    configureLogging()

    val parser = ArgParser("Hub")
    val dataDir by parser.option(ArgType.String).required()
    val root by parser.option(ArgType.String, description = "path to root certificate chain, if using TLS")
    val key by parser.option(ArgType.String, description = "path to secret key file, if using mutual TLS")
    val cert by parser.option(ArgType.String, description = "path to public certificate, if using mutual TLS")
    val config by parser.option(ArgType.String, description = "path to config file (overrides default)")

    var subcommandExecuted = false

    class MicrophoneRecording : Subcommand("record", "record audio from microphone") {
        override fun execute() {
            if (config != null) {
                CONFIG_FILENAME = config!!
            }

            val channel: GrpcChannelChoice = GrpcChannelChoice.fromArgs(root = root, cert = cert, key = key)

            runBlocking {
                runRecordingHub(dataDirectory = Path(dataDir), channelChoice = channel)
            }

            subcommandExecuted = true
        }
    }

    class PrerecordedAudio : Subcommand("file", "use audio from pre-recorded file") {
        val fileName by argument(ArgType.String, "the filename of the pre-recorded file")

        override fun execute() {
            TODO("TODO: will process $fileName")

            subcommandExecuted = true
        }
    }

    parser.subcommands(MicrophoneRecording(), PrerecordedAudio())
    parser.parse(args)

    if (!subcommandExecuted) {
        System.err.println("missing subcommand. Run program with -h or --help for usage information.")
    }
}
