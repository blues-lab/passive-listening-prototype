package plp.transcribe

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import plp.common.configureLogging
import plp.common.resolveHomeDirectory
import java.io.File

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
