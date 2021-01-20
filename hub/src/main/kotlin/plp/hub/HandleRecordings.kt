package plp.hub

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import plp.common.rpc.MutualAuthInfo
import plp.data.Database
import plp.logging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi

/** How long each recording should be, by default */
const val DEFAULT_DURATION_SECONDS = 5

private val logger = KotlinLogging.logger { }

data class RegisteredRecording(val recording: Recording, val id: Long)

@ExperimentalPathApi
@ExperimentalCoroutinesApi
fun CoroutineScope.registerRecordings(database: Database, recordings: ReceiveChannel<Recording>) = produce {
    for (recording in recordings) {
        val id = database.registerRecording(recording)
        send(RegisteredRecording(recording, id))
    }
}

@ExperimentalPathApi
@ExperimentalCoroutinesApi
fun CoroutineScope.transcribeRecordings(
    database: Database,
    transcriber: Transcriber,
    records: ReceiveChannel<RegisteredRecording>
) = produce {
    for (record in records) {
        val recording = record.recording
        logger.debug { "transcribing recording $recording" }

        try {
            val text = transcriber.transcribeFile(recording.path)
            database.saveTranscript(record, text)
        } catch (err: io.grpc.StatusException) {
            logger.error("transcription failed: $err")
        }

        send(record)
    }
}

@ExperimentalCoroutinesApi
@ExperimentalPathApi
fun launchRecordingPipeline(dataDirectory: Path, mutualAuthInfo: MutualAuthInfo, state: RecordingState): Job {
    val database = initDatabase(dataDirectory)
    val recorder = MultiSegmentRecorder(DEFAULT_RECORDER, DEFAULT_DURATION_SECONDS, dataDirectory)
    val transcriber = MutualAuthTranscriptionClient(mutualAuthInfo)

    logger.debug { "launching recording job" }
    val recordingJob = GlobalScope.launch {
        val newRecordings = recordContinuously(recorder, state)
        val registeredRecordings = registerRecordings(database, newRecordings)
        val transcribedRecordings = transcribeRecordings(database, transcriber, registeredRecordings)

        var i = 0
        transcribedRecordings.consumeEach { nextRecording ->
            logger.debug("finished processing recording $i of current session: $nextRecording")

            i++
        }
    }
    logger.debug { "recording job is now running in the background" }
    return recordingJob
}

@ExperimentalCoroutinesApi
@ExperimentalPathApi
fun runRecordingHub(dataDirectory: Path, mutualAuthInfo: MutualAuthInfo) = runBlocking {
    val state = RecordingState
    val recordingJob = launchRecordingPipeline(dataDirectory, mutualAuthInfo, state)

    val server = startWebserver()

    // Listen for user input to exit
    while (true) {
        println(
            "Recording is ${
            state.status.toString().toLowerCase()
            }. Type anything followed by Enter to toggle recording. Press CTRL-D to exit."
        )
        readLine() ?: break // break out of loop on EOF (CTRL-D)

        // Toggle recording status (on any other input)
        when (state.status) {
            RecordingStatus.ACTIVE -> {
                logger.info { "pausing recording" }
                state.status = RecordingStatus.PAUSED
            }
            RecordingStatus.PAUSED -> {
                logger.info { "restarting recording" }
                state.status = RecordingStatus.ACTIVE
            }
            RecordingStatus.CANCELED -> {
                logger.warning("recording status is unexpectedly ${state.status}")
            }
        }
    }

    // Gracefully stop recording
    logger.info { "stopping recording job" }
    state.status = RecordingStatus.CANCELED
    recordingJob.join()
    logger.info { "recording pipeline has been shut downt" }

    while (true) {
        println("Recording pipeline is shut down and won't start again. Server is still running. Press CTRL-D to stop it.")
        readLine() ?: break
    }

    logger.info { "stopping web server" }
    server.stop(WEB_SERVICE_SHUTDOWN_TIMEOUT_MS, WEB_SERVICE_SHUTDOWN_TIMEOUT_MS)

    logger.info("all done, exiting")
}
