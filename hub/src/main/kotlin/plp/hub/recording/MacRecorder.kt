package plp.hub.recording

import plp.common.runCommandAndGetOutput
import plp.logging.KotlinLogging

private val logger = KotlinLogging.logger {}

enum class OS {
    UNKNOWN, WINDOWS, LINUX, MAC, SOLARIS
}

/**
 * Get the current system's OS
 * via https://stackoverflow.com/a/31547504
 */
fun getOS(): OS {
    val os = System.getProperty("os.name").toLowerCase()
    return when {
        os.contains("win") -> {
            OS.WINDOWS
        }
        os.contains("nix") || os.contains("nux") || os.contains("aix") -> {
            OS.LINUX
        }
        os.contains("mac") -> {
            OS.MAC
        }
        os.contains("sunos") -> {
            OS.SOLARIS
        }
        else -> OS.UNKNOWN
    }
}

val RecordMac = RecordOnce { durationSeconds, path ->
    logger.info("recording $durationSeconds seconds using ffmpeg to $path")
    val os = getOS();
    val ffmpegEncodingFormat = when(os) {
        OS.LINUX -> "alsa"
        OS.MAC -> "avfoundation"
        else -> "unknown"
    }

    runCommandAndGetOutput(
        listOf(
            "ffmpeg",
            "-f",
            "alsa",
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
