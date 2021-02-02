package plp.hub.web

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.default
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.serialization.json
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import plp.hub.RecordingState
import plp.hub.RecordingStatus
import plp.logging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.div

const val WEB_SERVICE_PORT = 8080

const val WEB_SERVICE_SHUTDOWN_TIMEOUT_MS = 5000L

private val logger = KotlinLogging.logger { }

/** Path in the local filesystem to static assets containing the frontend */
@ExperimentalPathApi
val DASHBOARD_PATH: Path = Path(
    System.getenv("DASHBOARD_PATH")
        ?: "dashboard".also { logger.warning("DASHBOARD_PATH not specified in environment; using default path (./dashboard)") }
)

@ExperimentalPathApi
fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)

    install(ContentNegotiation) {
        json()
    }

    install(Routing) {
        static("dashboard") {
            files(DASHBOARD_PATH.toFile())
            default((DASHBOARD_PATH / "index.html").toFile())
        }

        returnRecordings()
        getRecordingAudio()

        get("/") {
            call.respondRedirect("/dashboard")
        }

        get("/recording/status") {
            val status = RecordingState.status.toString()
            call.respondText(status, ContentType.Text.Plain)
        }

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
}

@ExperimentalPathApi
fun startWebserver(): ApplicationEngine {
    logger.debug { "starting web server" }
    val server =
        embeddedServer(Netty, WEB_SERVICE_PORT, watchPaths = listOf("WebServiceKt"), module = Application::module)
    server.start()
    logger.debug { "started web server" }
    return server
}
