package plp.transcribe

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.coroutines.runBlocking
import plp.common.configureLogging
import plp.common.toPath
import kotlin.io.path.ExperimentalPathApi

@ExperimentalPathApi
fun main(args: Array<String>) {
    configureLogging()

    val parser = ArgParser("StandaloneWav2letter")
    val file by parser.option(ArgType.String).required()
    val model by parser.option(ArgType.String).required()
    parser.parse(args)

    val filePath = file.toPath()
    val modelPath = model.toPath()
    val transcriber = Wav2letterTranscriber(modelPath)
    val transcript: String = runBlocking { transcriber.transcribeFile(filePath) }
    println(transcript)
}
