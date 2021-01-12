package plp.ear

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import plp.common.configureLogging
import plp.common.rpc.MutualAuthInfo
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

@ExperimentalCoroutinesApi
@ExperimentalPathApi
fun main(args: Array<String>) = runBlocking {
    configureLogging()

    val parser = ArgParser("Main")
    val dataDir by parser.option(ArgType.String).required()
    val key by parser.option(ArgType.String).required()
    val cert by parser.option(ArgType.String).required()
    val root by parser.option(ArgType.String).required()
    parser.parse(args)

    val mutualAuthInfo = MutualAuthInfo(root = root, cert = cert, key = key)

    runRecordingHub(dataDirectory = Path(dataDir), mutualAuthInfo = mutualAuthInfo)
}
