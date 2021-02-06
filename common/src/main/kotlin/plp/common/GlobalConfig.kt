package plp.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hjson.JsonValue
import plp.logging.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

private const val LOCALHOST = "localhost"
private const val TRANSCRIPTION_SERVICE_PORT = 50057
private const val VAD_SERVICE_PORT = 50059
private const val SAMPLE_CLASSIFICATION_SERVICE_PORT = 50060
val DEFAULT_CONFIG: Config = Config(
    transcriptionService = Service(LOCALHOST, TRANSCRIPTION_SERVICE_PORT),
    vadService = Service(LOCALHOST, VAD_SERVICE_PORT),
    classificationServices = listOf(Service(LOCALHOST, SAMPLE_CLASSIFICATION_SERVICE_PORT)),
    dashboardCredentials = Credentials("test", "changeit")
)

/** The default location where to look for the config */
var CONFIG_FILENAME = "config.json"

@Serializable
data class Service(val host: String, val port: Int)

@Serializable
data class Credentials(val username: String, val password: String)

@Serializable
data class Config(
    val transcriptionService: Service,
    val vadService: Service,
    val classificationServices: List<Service>,
    val dashboardCredentials: Credentials,
)

/** Return the config loaded from default file name/location, or a default config if that doesn't exist */
fun loadGlobalConfig(): Config {
    val configFile = File(CONFIG_FILENAME)

    return if (configFile.exists()) {
        val configJson = JsonValue.readHjson(configFile.reader()).toString()
        val config = Json.decodeFromString<Config>(configJson)
        logger.debug { "successfully loaded config from `${configFile.absolutePath}`" }
        config
    } else {
        val defaultConfigSerialized = Json { prettyPrint = true }.encodeToString(DEFAULT_CONFIG)
        logger.warning(
            """couldn't locate config file `${configFile.absolutePath}`, using default:
```json
$defaultConfigSerialized
```"""
        )
        DEFAULT_CONFIG
    }
}

/** The default config for the project, containing service addresses and definitions */
val GLOBAL_CONFIG: Config by lazy(::loadGlobalConfig)
