package plp.hub.recording

import plp.common.OS
import plp.common.getOS
import plp.common.runCommandAndGetOutput
import plp.logging.KotlinLogging

private val logger = KotlinLogging.logger {}

val RecordMac: RecordOnce = run {
    val ffmpegEncodingFormat = when (getOS()) {
        OS.LINUX -> "alsa"
        OS.MAC -> "avfoundation"
        else -> "unknown"
    }
    logger.debug { "setting up FfmpegRecorder using $ffmpegEncodingFormat" }

    RecordOnce { durationSeconds, path ->
        logger.info("recording $durationSeconds seconds using ffmpeg to $path")

        runCommandAndGetOutput(
            listOf(
                "ffmpeg",
                "-f",
                ffmpegEncodingFormat,
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
}
