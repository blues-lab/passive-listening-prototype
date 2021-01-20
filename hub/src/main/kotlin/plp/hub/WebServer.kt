package plp.hub

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.serialization.json
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import plp.logging.KotlinLogging

const val WEB_SERVICE_PORT = 8080

const val WEB_SERVICE_SHUTDOWN_TIMEOUT_MS = 5000L

private val logger = KotlinLogging.logger { }

fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)

    install(ContentNegotiation) {
        json()
    }

    install(Routing) {
        returnAllRecordings()

        get("/") {
            call.respondText("Hello world", ContentType.Text.Html)
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
                RecordingStatus.PAUSED -> {
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
                    RecordingState.status = RecordingStatus.PAUSED
                    call.respondText("OK")
                }
                RecordingStatus.PAUSED -> {
                    call.respondText("No change") // TODO: provide standardized JSON response
                }
                RecordingStatus.CANCELED -> {
                    call.respondText("Pipeline stopped", status = HttpStatusCode.BadRequest)
                }
            }
            logger.debug { "new recording status is ${RecordingState.status}" }
        }
    }
}

fun startWebserver(): ApplicationEngine {
    logger.debug { "starting web server" }
    val server =
        embeddedServer(Netty, WEB_SERVICE_PORT, watchPaths = listOf("WebServiceKt"), module = Application::module)
    server.start()
    logger.debug { "started web server" }
    return server
}
