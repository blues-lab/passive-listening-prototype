package plp.ear

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import plp.data.Database
import plp.logging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.div

/** How long each recording should be, by default */
const val DEFAULT_DURATION_SECONDS = 5

private val logger = KotlinLogging.logger { }

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

        send(recording)
    }
}

@ExperimentalCoroutinesApi
@ExperimentalPathApi
fun runRecordingHub(dataDirectory: Path) {
    val database = initDatabase(dataDirectory)
    val recorder = MultiSegmentRecorder(DEFAULT_RECORDER, DEFAULT_DURATION_SECONDS, dataDirectory)

    logger.debug { "launching recording job" }
    val recordingJob = GlobalScope.launch {
        val newRecordings = recordContinuously(recorder)
        val registeredRecordings = registerRecordings(database, newRecordings)

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
