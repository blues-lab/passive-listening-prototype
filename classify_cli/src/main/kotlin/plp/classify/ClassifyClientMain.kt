package plp.classify

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import plp.common.rpc.GrpcChannelChoice
import kotlin.io.path.ExperimentalPathApi

@ExperimentalCoroutinesApi
@ExperimentalPathApi
fun main(args: Array<String>) = runBlocking {
    val parser = ArgParser("ClassifyClientMain")
    val text by parser.option(
        ArgType.String,
        shortName = "t",
        description = "the text you want to classify. If not specified, will read it from STDIN continuously."
    )
    val root by parser.option(ArgType.String, description = "path to root certificate chain, if using TLS")
    val key by parser.option(ArgType.String, description = "path to secret key file, if using mutual TLS")
    val cert by parser.option(ArgType.String, description = "path to public certificate, if using mutual TLS")
    parser.parse(args)

    val channel: GrpcChannelChoice = GrpcChannelChoice.fromArgs(root = root, cert = cert, key = key)
    val client = ClassificationClient(channel)

    if (text != null) {
        client.classifyText(text!!)
    } else {
        while (true) {
            println("Enter text you want to classify, or CTRL-D to exit.")
            val nextText = readLine() ?: break

            client.classifyText(nextText)
        }
    }
}
