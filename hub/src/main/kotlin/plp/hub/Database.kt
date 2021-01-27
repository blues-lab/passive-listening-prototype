package plp.hub

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import plp.data.Database
import plp.data.Transcript
import plp.hub.recording.Recording
import plp.logging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.div
import kotlin.io.path.nameWithoutExtension

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
fun getTimestampFromRecording(recording: Recording): Int {
    return recording.path.nameWithoutExtension.toInt()
}

/**
 * Save given recording in database
 * @return the ID of the new recording
 */
@ExperimentalPathApi
fun Database.registerRecording(recording: Recording): Long {
    val queries = this.audioQueries

    logger.debug { "registering recording $recording" }

    val filename = recording.path.fileName.toString()
    val timestamp = getTimestampFromRecording(recording)

    // Save recording to database
    queries.insert(
        filename,
        timestamp.toLong(),
        DEFAULT_DURATION_SECONDS.toDouble()
    ) // FIXME: use computed recording duration

    return queries.lastInsertRowId().executeAsOne()
}

/**
 * Save given transcription into the database
 */
@ExperimentalPathApi
fun Database.saveTranscript(recording: RegisteredRecording, text: String) {
    logger.debug { "saving transcript for $recording to database" }

    val queries = this.transcriptQueries

    val filename = recording.path.fileName.toString()
    val timestamp = getTimestampFromRecording(recording)

    queries.insert(
        recording.id,
        filename,
        timestamp.toDouble(),
        DEFAULT_DURATION_SECONDS.toDouble(),
        text
    ) // FIXME: use computed recording duration
}

fun Database.selectAfterTimestamp(cutoff: Int = 0): List<Transcript> {
    val queries = this.transcriptQueries

    return queries.selectAfterTimestamp(cutoff.toDouble()).executeAsList()
}
