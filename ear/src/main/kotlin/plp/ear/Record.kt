package plp.ear

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import mu.KotlinLogging
import plp.common.runCommandAndGetOutput
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.div

private const val RECORDING_FILE_EXTENSION = ".wav"

private val logger = KotlinLogging.logger {}

/**
 * Return the number of seconds since the Unix epoch
 */
fun currentUnixTime(): Int {
    return Instant.now().epochSecond.toInt()
}

@ExperimentalPathApi
fun pathToNextRecording(directory: Path): Path {
    val fileBase = currentUnixTime().toString()
    val filename = fileBase + RECORDING_FILE_EXTENSION
    return directory / filename
}

fun recordMac(durationSeconds: Int, path: Path) {
    logger.info("recording $durationSeconds seconds using ffmpeg to $path")

    runCommandAndGetOutput(
        listOf(
            "ffmpeg",
            "-f",
            "avfoundation",
            "-i",
            ":0",
            "-t",
            durationSeconds.toString(),
            "-ac",
            "1",
            "-ar",
            "16k",
            path.toString()
        )
    )

    logger.debug("recording finished $path")
}

/**
 * @return the path to the newly recorded file
 */
@ExperimentalPathApi
fun recordNext(durationSeconds: Int, containingDirectory: Path): Path {
    val path = pathToNextRecording(containingDirectory)
    recordMac(durationSeconds, path)
    return path
}

data class Recording(val path: Path)

@ExperimentalPathApi
class Recorder(private val segmentDuration: Int, private val targetDirectory: Path) {
    fun recordNext(): Recording {
        val path = recordNext(segmentDuration, targetDirectory)
        return Recording(path)
    }
}

@ExperimentalCoroutinesApi
@ExperimentalPathApi
fun CoroutineScope.recordContinuously(recorder: Recorder) = produce {
    while (true) {
        send(recorder.recordNext())
    }
}
