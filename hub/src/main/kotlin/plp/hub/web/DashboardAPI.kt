package plp.hub.web

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plp.data.Transcript
import plp.hub.RecordingState
import plp.hub.selectAfterTimestamp
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.div
import kotlin.io.path.exists

/**
 * A recording chunk, as expected by the dashboard
 */
@Serializable
private data class RecordingChunk(
    val id: Int,
    val timestamp: Int,
    @SerialName("audio_id") val audioId: Int,
    @SerialName("sub_id") val subId: Int,
    val filename: String,
    val duration: Float,
    val transcription: String,
)

/** Convert a transcript (as stored in the database) to a recording chunk (for the dashboard) */
private fun transcriptToChunk(transcript: Transcript): RecordingChunk {
    return transcript.run {
        RecordingChunk(
            id = id.toInt(),
            timestamp = timestamp?.toInt() ?: -1,
            audioId = audio_id.toInt(),
            subId = -1,
            filename = filename,
            duration = duration.toFloat(),
            transcription = text ?: "",
        )
    }
}

fun Route.returnRecordings() {
    get("/data") {
        val database = RecordingState.database
        if (database == null) {
            call.respondText(text = "Database not initialized", status = HttpStatusCode.InternalServerError)
            return@get
        }

        val cutoff: Int = call.request.queryParameters["cutoff"]?.toIntOrNull() ?: 0

        val chunks = database.selectAfterTimestamp(cutoff).map(::transcriptToChunk)

        call.respond(chunks)
    }
}

/** Route returning the contents of the specified audio file */
@ExperimentalPathApi
fun Route.getRecordingAudio() {
    get("/audio/{filename}") {
        val recordingFilename: String? = call.parameters["filename"]
        if (recordingFilename == null) {
            call.respond(HttpStatusCode.BadRequest, "missing filename in path")
            return@get
        }

        val recordingPath = RecordingState.audioFileDirectory / recordingFilename
        if (!recordingPath.exists()) {
            call.respond(HttpStatusCode.NotFound, "$recordingFilename not found")
            return@get
        }

        call.respondFile(RecordingState.audioFileDirectory.toFile(), recordingFilename)
    }
}
