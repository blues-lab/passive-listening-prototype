package plp.hub

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
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
    install(Routing) {
        get("/") {
            call.respondText("Hello world", ContentType.Text.Html)
        }
    }
}

fun startWebserver(): ApplicationEngine {
    logger.debug { "starting web server" }
    val server = embeddedServer(Netty, WEB_SERVICE_PORT, watchPaths = listOf("WebServiceKt"), module = Application::module)
    server.start()
    logger.debug { "started web server" }
    return server
}
