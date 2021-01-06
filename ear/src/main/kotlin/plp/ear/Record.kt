package plp.ear

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.flow
import mu.KotlinLogging
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.div

private const val RECORDING_FILE_EXTENSION = ".wav"

val DEFAULT_RECORDER = RecordJava

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

data class Recording(val path: Path)

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
fun CoroutineScope.recordContinuously(recorder: MultiSegmentRecorder) = produce {
    while (true) {
        send(recorder.recordNext())
    }
}

@ExperimentalPathApi
fun recordingFlow(recorder: MultiSegmentRecorder) = flow {
    while (true) {
        emit(recorder.recordNext())
    }
}
