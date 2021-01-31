package plp.hub.recording

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import plp.common.currentUnixTime
import plp.hub.RecordingState
import plp.hub.RecordingStatus
import plp.logging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.div

private const val RECORDING_FILE_EXTENSION = ".wav"

val DEFAULT_RECORDER = RecordJava

/** How many recordings to buffer before pausing recording */
const val RECORDING_CHANNEL_BUFFER_SIZE = 10

/** When recording is paused, we'll sleep this long until checking whether we should resume */
const val SLEEP_INTERVAL_WHEN_RECORDING_IS_PAUSED = 5000L

private val logger = KotlinLogging.logger {}

@ExperimentalPathApi
fun pathToNextRecording(directory: Path): Path {
    val fileBase = currentUnixTime().toString()
    val filename = fileBase + RECORDING_FILE_EXTENSION
    return directory / filename
}

fun interface RecordOnce {
    fun record(durationSeconds: Int, path: Path)
}

/**
 * @return the path to the newly recorded file
 */
@ExperimentalPathApi
fun recordNext(recorder: RecordOnce, durationSeconds: Int, containingDirectory: Path): Path {
    val path = pathToNextRecording(containingDirectory)
    recorder.record(durationSeconds, path)
    return path
}

/** A recording that has been made and saved to the filesystem */
open class Recording(val path: Path) {
    /** Return the recording's field valus, concatenated to a string */
    open fun fieldsToString(): String {
        return "path=$path"
    }

    override fun toString(): String {
        return this::class.simpleName + "(" + fieldsToString() + ")"
    }
}

@ExperimentalPathApi
class MultiSegmentRecorder(
    private val recorder: RecordOnce,
    private val segmentDuration: Int,
    private val targetDirectory: Path
) {
    fun recordNext(): Recording {
        val path = recordNext(recorder, segmentDuration, targetDirectory)
        return Recording(path)
    }
}

@ExperimentalCoroutinesApi
@ExperimentalPathApi
fun CoroutineScope.recordContinuously(recorder: MultiSegmentRecorder, state: RecordingState) =
    produce(capacity = RECORDING_CHANNEL_BUFFER_SIZE) {
        logger.debug("recording continuously")

        while (true) {
            logger.trace { "recording status is ${state.status}" }
            when (state.status) {
                RecordingStatus.ACTIVE -> send(recorder.recordNext())
                RecordingStatus.PAUSED -> {
                    logger.trace { "sleeping for $SLEEP_INTERVAL_WHEN_RECORDING_IS_PAUSED ms because recording is paused" }
                    delay(SLEEP_INTERVAL_WHEN_RECORDING_IS_PAUSED)
                }
                RecordingStatus.CANCELED -> break
            }
        }

        logger.info { "recording state is ${RecordingStatus.CANCELED}, returning" }
    }
