package plp.hub.web

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.basic
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
import plp.common.GLOBAL_CONFIG
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

    install(Authentication) {
        basic("passwordGate") {
            realm = "dashboard"
            validate { credentials ->
                if ((credentials.name == GLOBAL_CONFIG.dashboardCredentials.username) &&
                    (credentials.password == GLOBAL_CONFIG.dashboardCredentials.password)
                ) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }

    install(Routing) {
        static("dashboard") {
            files(DASHBOARD_PATH.toFile())
            default((DASHBOARD_PATH / "index.html").toFile())
        }
        authenticate("passwordGate") {

            returnRecordings()
            getRecordingAudio()

            get("/") {
                call.respondRedirect("/dashboard")
            }

            getRecordingStatus()
            startRecording()
            stopRecording()
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
