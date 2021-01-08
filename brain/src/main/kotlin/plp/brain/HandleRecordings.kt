package plp.brain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import plp.data.Database
import plp.logging.KotlinLogging
import plp.proto.AudioRecordingOuterClass
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.div
import kotlin.io.path.writeBytes

private val logger = KotlinLogging.logger {}

@ExperimentalPathApi
fun handleRecordings(audioDirectory: Path, database: Database, recordings: Flow<AudioRecordingOuterClass.Recording>) =
    runBlocking {
        val queries = database.audioQueries

        recordings.collect { recording: AudioRecordingOuterClass.Recording ->
            logger.info { "received recording from ${recording.timestamp}" }

            // Write audio to file
            val filename = "${recording.timestamp}.wav"
            val filePath = audioDirectory / filename
            filePath.writeBytes(recording.audio.toByteArray())
            logger.debug { "wrote ${recording.audio.size()} bytes to $filePath" }

            // Save recording to database
            queries.insert(
                filename,
                recording.timestamp.toLong(),
                RECORDING_SEGMENT_DURATION_SECONDS.toDouble()
            ) // FIXME: use computed recording duration
        }
    }
