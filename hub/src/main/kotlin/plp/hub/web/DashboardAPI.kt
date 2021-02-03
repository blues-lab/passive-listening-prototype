package plp.hub.web

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import plp.hub.RecordingState
import plp.hub.database.selectAfterTimestamp
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.div
import kotlin.io.path.exists

fun Route.returnRecordings() {
    get("/data") {
        val database = RecordingState.database
        if (database == null) {
            call.respondText(text = "Database not initialized", status = HttpStatusCode.InternalServerError)
            return@get
        }

        val cutoff = call.request.queryParameters["cutoff"]?.toLongOrNull() ?: 0

        val data = database.selectAfterTimestamp(cutoff)

        call.respond(data)
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
