package plp.ear

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import plp.common.configureLogging
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

@ExperimentalCoroutinesApi
@ExperimentalPathApi
fun main(args: Array<String>) = runBlocking {
    configureLogging()

    val parser = ArgParser("StandaloneContinuousRecorder")
    val path by parser.option(ArgType.String).required()
    val duration by parser.option(ArgType.Int).required()
    parser.parse(args)

    val containingDirectory = Path(path)
    val recorder = MultiSegmentRecorder(DEFAULT_RECORDER, duration, containingDirectory)
    val newRecordings = recordContinuously(recorder)

    @Suppress("MagicNumber")
    val count = 3

    repeat(count) {
        println("recording $it/3")
        val nextRecording = newRecordings.receive()
        println("finished recording $nextRecording")
    }
    newRecordings.cancel()
    println("all done")
}
