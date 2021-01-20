package plp.hub

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plp.data.Transcripts

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
private fun transcriptToChunk(transcript: Transcripts): RecordingChunk {
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

fun Route.returnAllRecordings() {
    get("/recording/all") {
        val database = RecordingState.database
        if (database == null) {
            call.respondText(text = "Database not initialized", status = HttpStatusCode.InternalServerError)
            return@get
        }

        val chunks = database.selectAll().map(::transcriptToChunk)

        call.respond(chunks)
    }
}
