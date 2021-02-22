package plp.transcribe.aws

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import plp.common.CONFIG_FILENAME
import java.io.File

@Serializable
data class AwsConfig(val bucket: Bucket)

var AWS_CONFIG_FILENAME = CONFIG_FILENAME

private fun getConfig(): AwsConfig {
    val configFile = File(AWS_CONFIG_FILENAME)
    if (!configFile.exists()) {
        throw RuntimeException(
            "missing config file (expected ${configFile.absolutePath}) that should look like this: " +
                """{"bucket": "BUCKET_NAME"}"""
        )
    }
    val configContents = configFile.readText()
    return Json { ignoreUnknownKeys }.decodeFromString(configContents)
}

fun getConfiguredBucket(): Bucket {
    return getConfig().bucket
}