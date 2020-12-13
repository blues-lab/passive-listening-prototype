package plp.ear

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import plp.common.configureLogging
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

@ExperimentalPathApi
fun main(args: Array<String>) {
    configureLogging()

    val parser = ArgParser("StandaloneRecorder")
    val path by parser.option(ArgType.String).required()
    val duration by parser.option(ArgType.Int).required()
    parser.parse(args)

    val containingDirectory = Path(path)
    recordNext(duration, containingDirectory)
}
