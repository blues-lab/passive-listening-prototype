package plp.hub

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import plp.common.configureLogging
import plp.common.rpc.GrpcChannelChoice
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

@ExperimentalCoroutinesApi
@ExperimentalPathApi
fun main(args: Array<String>) = runBlocking {
    configureLogging()

    val parser = ArgParser("Main")
    val dataDir by parser.option(ArgType.String).required()
    val root by parser.option(ArgType.String, description = "path to root certificate chain, if using TLS")
    val key by parser.option(ArgType.String, description = "path to secret key file, if using mutual TLS")
    val cert by parser.option(ArgType.String, description = "path to public certificate, if using mutual TLS")
    parser.parse(args)

    val channel: GrpcChannelChoice = GrpcChannelChoice.fromArgs(root = root, cert = cert, key = key)

    runRecordingHub(dataDirectory = Path(dataDir), channelChoice = channel)
}
