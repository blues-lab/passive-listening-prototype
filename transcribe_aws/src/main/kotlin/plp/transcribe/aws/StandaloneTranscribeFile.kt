package plp.transcribe.aws

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.coroutines.runBlocking
import plp.common.toPath
import kotlin.io.path.ExperimentalPathApi

@ExperimentalPathApi
fun main(args: Array<String>) {
    val parser = ArgParser("TranscribeFile")
    val file by parser.option(ArgType.String).required()
    parser.parse(args)

    val filePath = file.toPath()

    val bucket = getConfiguredBucket()
    val s3 = S3(bucket)
    val transcriber = AwsTranscribe(s3)
    val transcript: String = runBlocking { transcriber.transcribeFile(filePath) }
    println(transcript)
}
