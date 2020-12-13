package plp.ear

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
    logger.info("recording %d seconds using ffmpeg to %s", durationSeconds, path)

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

    logger.debug("recording finished %s", path)
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
