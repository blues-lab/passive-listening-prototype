package plp.hub.web

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.mustache.MustacheContent
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import plp.hub.RecordingState
import plp.hub.RecordingStatus
import plp.hub.database.selectAfterTimestamp
import plp.logging.KotlinLogging
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.div
import kotlin.io.path.exists

private val logger = KotlinLogging.logger { }

/** Reset the template engine's cache */
fun Route.clearTemplateCache() {
    get("/_reset_templates") {
        serverTemplateEngine?.clearCache()
        call.respondText("Template cache cleared", ContentType.Text.Plain)
    }
}

fun Route.showDashboard() {
    get("/") {
        val database = RecordingState.database
        if (database == null) {
            call.respondText(text = "Database not initialized", status = HttpStatusCode.InternalServerError)
            return@get
        }

        val cutoff = call.request.queryParameters["cutoff"]?.toLongOrNull() ?: 0

        val data = database.selectAfterTimestamp(cutoff)

        call.respond(
            MustacheContent(
                "index.html",
                mapOf(
                    "recordings" to data
                )
            )
        )
    }
}

fun Route.getRecordingStatus() {
    get("/recording/status") {
        val status = RecordingState.status.toString()
        call.respondText(status, ContentType.Text.Plain)
    }
}

fun Route.startRecording() {
    post("/recording/start") {
        logger.debug { "received start request; current status is ${RecordingState.status}" }
        when (RecordingState.status) {
            RecordingStatus.ACTIVE -> {
                call.respondText("No change") // TODO: provide standardized JSON response
            }
            RecordingStatus.PAUSING, RecordingStatus.PAUSED, RecordingStatus.CANCELING -> {
                RecordingState.status = RecordingStatus.ACTIVE
                call.respondText("OK")
            }
            RecordingStatus.CANCELED -> {
                call.respondText("Pipeline stopped", status = HttpStatusCode.BadRequest)
            }
        }
        logger.debug { "new recording status is ${RecordingState.status}" }
    }
}

fun Route.stopRecording() {
    post("/recording/stop") {
        logger.debug { "received start request; current status is ${RecordingState.status}" }
        when (RecordingState.status) {
            RecordingStatus.ACTIVE -> {
                RecordingState.status = RecordingStatus.PAUSING
                call.respondText("OK")
            }
            RecordingStatus.PAUSING, RecordingStatus.PAUSED -> {
                call.respondText("No change") // TODO: provide standardized JSON response
            }
            RecordingStatus.CANCELING, RecordingStatus.CANCELED -> {
                call.respondText("Pipeline stopped", status = HttpStatusCode.BadRequest)
            }
        }
        logger.debug { "new recording status is ${RecordingState.status}" }
    }
}

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
