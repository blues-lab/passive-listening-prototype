package plp.hub.web

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.mustache.MustacheContent
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import plp.common.GLOBAL_CONFIG
import plp.common.rpc.client.GrpcChannelChoice
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
        call.respond(
            MustacheContent(
                "index.html",
                emptyMap<String, String>()
            )
        )
    }
}

fun Route.showDisplay() {
    get("/display") {
        call.respond(
            MustacheContent(
                "display.html",
                emptyMap<String, String>()
            )
        )
    }
}

fun Route.loadDashboardData(channelChoice: GrpcChannelChoice) {
    get("/api/load_dashboard_data") {
        val dashboardQueries: DashboardClientList =
            GLOBAL_CONFIG.classificationServices.map {
                service ->
                DashboardClient(channelChoice, service)
            }
        val result = dashboardQueries.map {
            dashboard_request ->
            dashboard_request.queryDashboardData()
        }
        call.respond(
            MustacheContent(
                "display_details.html",
                mapOf("dashboardData" to result)
            )
        )
    }
}

fun Route.renderRecordings() {
    get("/api/recordings") {
        val database = RecordingState.database
        if (database == null) {
            call.respondText(text = "Database not initialized", status = HttpStatusCode.InternalServerError)
            return@get
        }

        val cutoff = call.request.queryParameters["cutoff"]?.toLongOrNull() ?: 0

        val data = database.selectAfterTimestamp(cutoff)

        call.respond(
            MustacheContent(
                "recordings.html",
                mapOf(
                    "recordings" to data
                )
            )
        )
    }
}

fun Route.showRecordingControlButton() {
    get("/api/recording/control") {
        when (RecordingState.status) {
            RecordingStatus.ACTIVE -> call.respond(
                MustacheContent(
                    "recording_control_pause.html",
                    emptyMap<String, Unit>()
                )
            )
            RecordingStatus.PAUSED -> call.respond(
                MustacheContent(
                    "recording_control_start.html",
                    emptyMap<String, Unit>()
                )
            )
            else -> call.respondText("", ContentType.Text.Html)
        }
    }
}

fun Route.startRecording() {
    post("/api/recording/start") {
        logger.debug { "received start request; current status is ${RecordingState.status}" }
        when (RecordingState.status) {
            RecordingStatus.PAUSING, RecordingStatus.PAUSED, RecordingStatus.CANCELING -> {
                RecordingState.status = RecordingStatus.ACTIVE
            }
            else -> {
                logger.debug { "received stop request, but current status is ${RecordingState.status}. no change." }
            }
        }
        logger.debug { "new recording status is ${RecordingState.status}" }
        call.respondRedirect("/api/recording/control")
    }
}

fun Route.stopRecording() {
    post("/api/recording/stop") {
        logger.debug { "received start request; current status is ${RecordingState.status}" }
        when (RecordingState.status) {
            RecordingStatus.ACTIVE -> {
                RecordingState.status = RecordingStatus.PAUSING
            }
            else -> {
                logger.debug { "received stop request, but current status is ${RecordingState.status}. no change." }
            }
        }
        logger.debug { "new recording status is ${RecordingState.status}" }
        call.respondRedirect("/api/recording/control")
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
