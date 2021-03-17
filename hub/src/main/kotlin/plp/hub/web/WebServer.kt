package plp.hub.web

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.content.default
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.mustache.Mustache
import io.ktor.routing.Routing
import io.ktor.serialization.json
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import plp.logging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.div

const val WEB_SERVICE_PORT = 8080

const val WEB_SERVICE_SHUTDOWN_TIMEOUT_MS = 5000L

/**
 * The name to use for the authentication config.
 * These are supposed to distinguish the different configurations,
 * but we only have one (right now), so it doesn't really matter.
 */
const val AUTHENTICATION_GROUP = "passwordGate"

private val logger = KotlinLogging.logger { }

/** Path in the local filesystem to static assets containing the frontend */
@ExperimentalPathApi
val DASHBOARD_PATH: Path = Path(
    System.getenv("DASHBOARD_PATH")
        ?: "dashboard".also { logger.warning("DASHBOARD_PATH not specified in environment; using default path (./dashboard)") }
)

/**
 * The template engine used by the server.
 * It's made available here, so that routes can access it for cache invalidation
 * (a somewhat ugly solution that helps debugging).
 */
var serverTemplateEngine: InvalidatableMustacheFactory? = null

@ExperimentalPathApi
fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)

    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        enableBasicAuthentication(AUTHENTICATION_GROUP)
    }

    install(Mustache) {
        val factory = InvalidatableMustacheFactory("templates")
        this.mustacheFactory = factory
        serverTemplateEngine = factory
    }

    install(Routing) {
        static("dashboard") {
            files(DASHBOARD_PATH.toFile())
            default((DASHBOARD_PATH / "index.html").toFile())
        }
        authenticate(AUTHENTICATION_GROUP) {
            showDashboard()
            returnRecordings()
            getRecordingAudio()
            getRecordingStatus()
            startRecording()
            stopRecording()

            // Debug paths
            clearTemplateCache()
        }
    }
}

@ExperimentalPathApi
fun startWebserver(): ApplicationEngine {
    logger.debug { "starting web server" }
    val server =
        embeddedServer(
            Netty,
            WEB_SERVICE_PORT,
            // watchPaths = listOf("web"), // disabled because it doesn't seem to pick up on the current file's path. Instead all changes should be picked up, which is fine.
            module = Application::module
        )
    server.start()
    logger.debug { "started web server" }
    return server
}
