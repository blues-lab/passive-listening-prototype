package plp.hub.database

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import plp.data.Database
import plp.hub.DEFAULT_DURATION_SECONDS
import plp.hub.recording.Recording
import plp.hub.transcription.TranscribedRecording
import plp.logging.KotlinLogging
import plp.proto.Classification
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

/**
 * If a recording's timestamp is not available, use this value
 */
const val DEFAULT_TIMESTAMP_WHEN_NOT_AVAILABLE = 0

/**
 * Infer timestamp for recording's filename
 * (Only works with recordings created with internal plp.hub.recording.Recording implementations)
 * @see plp.hub.recording.pathToNextRecording
 */
@ExperimentalPathApi
fun getTimestampFromRecording(recording: Recording): Int {
    val inferredTimestamp = recording.path.nameWithoutExtension.toIntOrNull()
    if (inferredTimestamp == null) {
        logger.error { "failed to infer timestamp from recording's path (will use timestamp 0): ${recording.path}" }
        return DEFAULT_TIMESTAMP_WHEN_NOT_AVAILABLE
    }
    return inferredTimestamp
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
fun Database.saveTranscript(recording: RegisteredRecording, text: String): Long {
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
    ) // TODO: use computed recording duration

    return queries.lastInsertRowId().executeAsOne()
}

/**
 * Return recordings (with additional data) after the given timestamp
 *
 * XXX: this returns a new row for each classification, so each audio recording is likely to result in multiple instances.
 * For now, the client is expected to handle this, but we may consider handling it here in the future.
 */
fun Database.selectAfterTimestamp(cutoff: Long = 0): List<AudioWithClassification> {
    val queries = this.classificationQueries
    val allAudio = queries.selectAllAudio(cutoff).executeAsList()
    return allAudio.map { it.toSerializable() }
}

/**
 * Save given classification into the database
 */
fun Database.saveClassification(
    recording: TranscribedRecording,
    classification: Classification.ClassificationResponse
) {
    logger.debug { "saving classification of $recording to database" }

    val queries = this.classificationQueries

    queries.insert(
        transcript_id = recording.transcriptId,
        classifier = classification.classifierName,
        classification = classification.classification,
        confidence = classification.confidence.toDouble(),
        extras = classification.extras,
    )
}
