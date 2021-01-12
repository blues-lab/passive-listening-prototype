package plp.ear

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import plp.logging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi

/** How long each recording should be, by default */
const val DEFAULT_DURATION_SECONDS = 5

private val logger = KotlinLogging.logger { }

@ExperimentalCoroutinesApi
fun CoroutineScope.registerRecordings(recordings: ReceiveChannel<Recording>) = produce {
    for (recording in recordings) {
        logger.debug { "registering recording $recording" }
        send(recording)
    }
}

@ExperimentalCoroutinesApi
@ExperimentalPathApi
fun runRecordingHub(dataDirectory: Path) {
    val recorder = MultiSegmentRecorder(DEFAULT_RECORDER, DEFAULT_DURATION_SECONDS, dataDirectory)

    logger.debug { "launching recording job" }
    val recordingJob = GlobalScope.launch {
        val newRecordings = recordContinuously(recorder)
        val registeredRecordings = registerRecordings(newRecordings)

        var i = 0
        registeredRecordings.consumeEach { nextRecording ->
            logger.debug("finished processing recording $i of current session: $nextRecording")

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
