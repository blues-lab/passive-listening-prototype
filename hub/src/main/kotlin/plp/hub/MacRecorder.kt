package plp.hub

import plp.common.runCommandAndGetOutput
import plp.logging.KotlinLogging

private val logger = KotlinLogging.logger {}

val RecordMac = RecordOnce { durationSeconds, path ->
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
