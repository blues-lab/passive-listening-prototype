package plp.brain

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import java.io.File

/**
 * Replace ~ in given string with user's home directory
 */
fun resolveHomeDirectory(path: String): String {
    return path.replaceFirst("~", System.getProperty("user.home"))
}

fun main(args: Array<String>) {
    configureLogging()

    val parser = ArgParser("TranscriptionServer")
    val file by parser.option(ArgType.String).required()
    val model by parser.option(ArgType.String).required()
    parser.parse(args)

    val filePath = File(resolveHomeDirectory(file))
    val modelPath = File(resolveHomeDirectory(model))
    val transcript = transcribeFile(filePath, modelPath)
    println(transcript)
}
