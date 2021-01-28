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
import plp.common.rpc.GrpcChannelChoice
import plp.data.Database
import plp.hub.classify.ClassificationClient
import plp.hub.recording.DEFAULT_RECORDER
import plp.hub.recording.MultiSegmentRecorder
import plp.hub.recording.Recording
import plp.hub.recording.recordContinuously
import plp.hub.transcription.Transcriber
import plp.hub.transcription.TranscriptionClient
import plp.hub.web.WEB_SERVICE_SHUTDOWN_TIMEOUT_MS
import plp.hub.web.startWebserver
import plp.logging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi

/** How long each recording should be, by default */
const val DEFAULT_DURATION_SECONDS = 5

private val logger = KotlinLogging.logger { }

open class RegisteredRecording(recording: Recording, val id: Long) : Recording(recording.path) {
    override fun fieldsToString(): String {
        return super.fieldsToString() + ", id=$id"
    }
}

open class TranscribedRecording(recording: RegisteredRecording, val transcription: String, val transcriptId: Long) :
    RegisteredRecording(recording, recording.id) {
    override fun fieldsToString(): String {
        return super.fieldsToString() + ", transcription=$transcription"
    }
}

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
    for (recording in records) {
        logger.debug { "transcribing recording $recording" }

        try {
            val text = transcriber.transcribeFile(recording.path)
            val transcriptId = database.saveTranscript(recording, text)
            send(TranscribedRecording(recording, text, transcriptId))
        } catch (err: io.grpc.StatusException) {
            logger.error("transcription failed: $err")
            send(recording)
        }
    }
}

@ExperimentalPathApi
@ExperimentalCoroutinesApi
fun CoroutineScope.classifyRecordings(
    database: Database,
    classifier: ClassificationClient,
    recordings: ReceiveChannel<RegisteredRecording>
) = produce {
    for (recording in recordings) {
        logger.debug { "classifying recording $recording" }

        if (recording is TranscribedRecording) {
            try {
                val classification = classifier.classifyRecording(recording)
                database.saveClassification(recording, classification)
            } catch (err: io.grpc.StatusException) {
                logger.error("classification failed: $err")
            }
        } else {
            logger.debug { "recording hasn't been transcribed, skipping classification" }
        }

        send(recording)
    }
}

@ExperimentalCoroutinesApi
@ExperimentalPathApi
fun launchRecordingPipeline(dataDirectory: Path, channelChoice: GrpcChannelChoice, state: RecordingState): Job {
    val database = initDatabase(dataDirectory)
    state.database = database
    val recorder = MultiSegmentRecorder(DEFAULT_RECORDER, DEFAULT_DURATION_SECONDS, dataDirectory)
    val transcriber = TranscriptionClient(channelChoice)
    val classifier = ClassificationClient(channelChoice)

    logger.debug { "launching recording job" }
    val recordingJob = GlobalScope.launch {
        val newRecordings = recordContinuously(recorder, state)
        val registeredRecordings = registerRecordings(database, newRecordings)
        val transcribedRecordings = transcribeRecordings(database, transcriber, registeredRecordings)
        val classifiedRecordings = classifyRecordings(database, classifier, transcribedRecordings)

        var i = 0
        classifiedRecordings.consumeEach { nextRecording ->
            logger.debug("finished processing recording $i of current session: $nextRecording")

            i++
        }
    }
    logger.debug { "recording job is now running in the background" }
    return recordingJob
}

/**
 * Toggle recording status based on STDIN input.
 * Returns only after receiving EOF (CTRL-D).
 */
private fun blockForUserRecordingControl(state: RecordingState) {
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
}

/**
 * The hub's main method:
 * - launches the recording pipeline
 * - starts the web server
 * - listens for STDIN input to control pipeline
 * - cleans up before exiting
 */
@ExperimentalCoroutinesApi
@ExperimentalPathApi
fun runRecordingHub(dataDirectory: Path, channelChoice: GrpcChannelChoice) = runBlocking {
    val state = RecordingState
    state.audioFileDirectory = dataDirectory
    val recordingJob = launchRecordingPipeline(dataDirectory, channelChoice, state)

    val server = startWebserver()

    // Control state based on user input
    blockForUserRecordingControl(state)

    // Gracefully stop recording
    logger.info { "stopping recording job" }
    state.status = RecordingStatus.CANCELED
    recordingJob.join()
    logger.info { "recording pipeline has been shut down" }

    while (true) {
        println("Recording pipeline is shut down and won't start again. Server is still running. Press CTRL-D to stop it.")
        readLine() ?: break
    }

    logger.info { "stopping web server" }
    server.stop(WEB_SERVICE_SHUTDOWN_TIMEOUT_MS, WEB_SERVICE_SHUTDOWN_TIMEOUT_MS)

    logger.info("recording pipeline is finished, exiting")
}
