package plp.hub.recording

import plp.common.OS
import plp.common.getOS
import plp.common.runCommandAndGetOutput
import plp.logging.KotlinLogging

private val logger = KotlinLogging.logger {}

val RecordFfmpeg: RecordOnce = run {
    val ffmpegEncodingFormat = when (val os = getOS()) {
        OS.LINUX -> "alsa"
        OS.MAC -> "avfoundation"
        else -> throw AudioRecordingException("no known ffmpeg format for OS $os")
    }
    logger.debug { "setting up FfmpegRecorder using $ffmpegEncodingFormat" }

    RecordOnce { durationSeconds, path ->
        logger.info { "recording $durationSeconds seconds using ffmpeg to $path" }

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

        logger.debug { "recording finished $path" }
    }
}
