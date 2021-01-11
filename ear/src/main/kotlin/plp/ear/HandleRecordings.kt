package plp.ear

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import plp.logging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi

const val DEFAULT_DURATION_SECONDS = 5

private val logger = KotlinLogging.logger { }

@ExperimentalCoroutinesApi
@ExperimentalPathApi
fun runRecordingHub(dataDirectory: Path) {
    val recorder = MultiSegmentRecorder(DEFAULT_RECORDER, DEFAULT_DURATION_SECONDS, dataDirectory)

    logger.debug { "launching recording job" }
    val recordingJob = GlobalScope.launch {
        val newRecordings = recordContinuously(recorder)

        var i = 0
        newRecordings.consumeEach { nextRecording ->
            logger.debug("finished recording $i of current session: $nextRecording")

            i++
        }
    }

    // Listen for user input to exit
    while (true) {
        println("Recording is running. Press CTRL-D to exit")
        readLine() ?: break
    }

    // Clean up
    logger.debug { "Stopping recording job" }
    recordingJob.cancel()

    logger.info("all done")
}
