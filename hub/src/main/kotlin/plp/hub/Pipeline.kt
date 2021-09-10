package plp.hub

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import plp.common.GLOBAL_CONFIG
import plp.common.rpc.client.GrpcChannelChoice
import plp.data.Database
import plp.hub.classify.ClassificationClient
import plp.hub.classify.ClassificationClientList
import plp.hub.database.RegisteredRecording
import plp.hub.database.initDatabase
import plp.hub.database.registerRecording
import plp.hub.database.saveClassification
import plp.hub.database.saveTranscript
import plp.hub.recording.DEFAULT_RECORDER
import plp.hub.recording.MultiSegmentRecorder
import plp.hub.recording.Recording
import plp.hub.recording.recordContinuously
import plp.hub.transcription.TranscribedRecording
import plp.hub.transcription.Transcriber
import plp.hub.transcription.TranscriptionClient
import plp.hub.vad.VadClient
import plp.hub.web.DashboardClient
import plp.hub.web.DashboardClientList
import plp.hub.web.WEB_SERVICE_SHUTDOWN_TIMEOUT_MS
import plp.hub.web.startWebserver
import plp.logging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi

/** How long each recording should be, by default */
const val DEFAULT_DURATION_SECONDS = 5

private val logger = KotlinLogging.logger { }

@ExperimentalPathApi
fun registerRecording(database: Database, recording: Recording): RegisteredRecording {
    val id = database.registerRecording(recording)
    return RegisteredRecording(recording, id)
}

@ExperimentalPathApi
suspend fun transcribeRecording(
    database: Database,
    transcriber: Transcriber,
    recording: RegisteredRecording,
): RegisteredRecording {
    logger.debug { "transcribing recording $recording" }

    return try {
        val text = transcriber.transcribeFile(recording.path)
        val transcriptId = database.saveTranscript(recording, text)
        TranscribedRecording(recording, text, transcriptId)
    } catch (err: io.grpc.StatusException) {
        logger.error("transcription of $recording failed: $err")
        recording
    }
}

@ExperimentalPathApi
suspend fun checkRecordingForSpeech(
    vad: VadClient,
    recording: RegisteredRecording,
): Boolean {
    return try {
        vad.recordingHasSpeech(recording)
    } catch (err: io.grpc.StatusException) {
        logger.error("voice activity detection failed for $recording (will treat recording as having speech): $err")
        true // to be safe, if vad fails, assume it might have speech
    }
}

@ExperimentalPathApi
fun CoroutineScope.classifyRecording(
    database: Database,
    classifiers: ClassificationClientList,
    recording: RegisteredRecording
): RegisteredRecording {
    logger.debug { "classifying recording $recording" }

    if (recording is TranscribedRecording) {
        if (recording.usableTranscription) {
            classifiers.map { classifier ->
                launch {
                    try {
                        val classification = classifier.classifyRecording(recording)
                        database.saveClassification(recording, classification)
                    } catch (err: io.grpc.StatusException) {
                        logger.error("classification of $recording failed: $err")
                    }
                }
            }
        } else {
            logger.debug { "recording $recording has no usable transcription, skipping classification" }
        }
    } else {
        logger.debug { "recording $recording hasn't been transcribed, skipping classification" }
    }

    return recording
}

@ExperimentalPathApi
fun CoroutineScope.launchJobToHandleRecording(
    database: Database,
    transcriber: Transcriber,
    vad: VadClient,
    classifiers: ClassificationClientList,
    newRecording: Recording
) = launch {
    logger.trace { "inside the new coroutine job to handle pipeline for recording $newRecording" }

    var recording = registerRecording(database, newRecording)
    logger.debug { "registered $newRecording as $recording " }

    if (checkRecordingForSpeech(vad, recording)) {
        recording = transcribeRecording(database, transcriber, recording)
        classifyRecording(database, classifiers, recording)
    } else {
        logger.debug { "recording $recording has no speech, skipping transcription and classification" }
    }
}

@ExperimentalCoroutinesApi
@ExperimentalPathApi
fun launchRecordingPipeline(dataDirectory: Path, channelChoice: GrpcChannelChoice, state: RecordingState): Job {
    val database = initDatabase(dataDirectory)
    state.database = database
    val recorder = MultiSegmentRecorder(DEFAULT_RECORDER, DEFAULT_DURATION_SECONDS, dataDirectory)
    val transcriber = TranscriptionClient(channelChoice)
    val vad = VadClient(channelChoice)

    val classifiers: ClassificationClientList =
        GLOBAL_CONFIG.classificationServices.map { service -> ClassificationClient(channelChoice, service) }

    logger.trace { "launching recording pipeline in a new job" }
    val recordingJob = GlobalScope.launch {
        logger.debug { "started the new recording pipeline job" }

        val newRecordings = recordContinuously(recorder, state)
        var i = 0

        for (newRecording in newRecordings) {
            logger.trace { "received new recording $newRecording in main recording pipeline job. launching new job to handle it. " }

            launchJobToHandleRecording(database, transcriber, vad, classifiers, newRecording).invokeOnCompletion {
                logger.debug("finished processing recording $i of current session: $newRecording")
                i++
            }

            logger.trace { "done launching coroutine for handling recording $newRecording" }
        }
    }

    logger.trace { "done launching new recording pipeline job" }

    return recordingJob
}

@ExperimentalCoroutinesApi
@ExperimentalPathApi
fun runPipelineForRecordings(
    dataDirectory: Path,
    channelChoice: GrpcChannelChoice,
    state: RecordingState,
    recordings: Iterable<Recording>
): Job {
    val database = initDatabase(dataDirectory)
    state.database = database
    val transcriber = TranscriptionClient(channelChoice)
    val vad = VadClient(channelChoice)

    val classifiers: ClassificationClientList =
        GLOBAL_CONFIG.classificationServices.map { service -> ClassificationClient(channelChoice, service) }

    val dashboardListeners: DashboardClientList = GLOBAL_CONFIG.classificationServices.map { service -> DashboardClient(channelChoice, service) }

    logger.trace { "launching recording pipeline in a new job" }
    val recordingJob = GlobalScope.launch {
        logger.debug { "started the new recording pipeline job" }

        var i = 0

        for (newRecording in recordings) {
            logger.trace { "received new recording $newRecording in main recording pipeline job. launching new job to handle it. " }

            launchJobToHandleRecording(database, transcriber, vad, classifiers, newRecording).invokeOnCompletion {
                logger.debug("finished processing recording $i of current session: $newRecording")
                i++
            }

            logger.trace { "done launching coroutine for handling recording $newRecording" }
        }
    }

    logger.trace { "done launching new recording pipeline job" }

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
                state.status = RecordingStatus.PAUSING
            }
            RecordingStatus.PAUSING -> {
                logger.warning { "recording is in the process of being paused, please wait" }
            }
            RecordingStatus.PAUSED -> {
                logger.info { "restarting recording" }
                state.status = RecordingStatus.ACTIVE
            }
            RecordingStatus.CANCELING, RecordingStatus.CANCELED -> {
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

    val server = startWebserver(channelChoice)

    // Control state based on user input
    blockForUserRecordingControl(state)

    // Gracefully stop recording
    logger.info { "stopping recording job" }
    state.status = RecordingStatus.CANCELING
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
