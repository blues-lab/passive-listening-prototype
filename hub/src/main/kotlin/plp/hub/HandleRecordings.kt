package plp.hub

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
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
import kotlin.io.path.div

/** How long each recording should be, by default */
const val DEFAULT_DURATION_SECONDS = 5

private val logger = KotlinLogging.logger { }

data class RegisteredRecording(val recording: Recording, val id: Long)

@ExperimentalPathApi
fun initDatabase(dataPath: Path): Database {
    // Create database paths and objects
    val dbPath = dataPath / "db.sqlite"
    val dbConnection = "jdbc:sqlite:$dbPath"
    val driver: SqlDriver = JdbcSqliteDriver(dbConnection)
    val database = Database(driver)

    // Make sure database is initialized with schema
    Database.Schema.create(driver)

    return database
}

@ExperimentalPathApi
@ExperimentalCoroutinesApi
fun CoroutineScope.registerRecordings(database: Database, recordings: ReceiveChannel<Recording>) = produce {
    val queries = database.audioQueries

    for (recording in recordings) {
        logger.debug { "registering recording $recording" }

        val filename = recording.path.fileName.toString()
        val timestamp = getTimestampFromRecording(recording)

        // Save recording to database
        queries.insert(
            filename,
            timestamp.toLong(),
            DEFAULT_DURATION_SECONDS.toDouble()
        ) // FIXME: use computed recording duration

        val id = queries.lastInsertRowId().executeAsOne()

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
    val queries = database.transcriptQueries

    for (record in records) {
        val recording = record.recording
        logger.debug { "transcribing recording $recording" }

        try {

            // Get transcript
            val text = transcriber.transcribeFile(recording.path)

            // Save recording to database
            val filename = recording.path.fileName.toString()
            val timestamp = getTimestampFromRecording(recording)

            queries.insert(
                record.id,
                filename,
                timestamp.toDouble(),
                DEFAULT_DURATION_SECONDS.toDouble(),
                text
            ) // FIXME: use computed recording duration
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
        println("Recording is running. Press CTRL-D to exit")
        readLine() ?: break
    }

    // Gracefully stop recording
    logger.info { "stopping recording job" }
    state.status = RecordingStatus.STOPPED
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
