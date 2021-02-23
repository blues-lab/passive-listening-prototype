package plp.transcribe.aws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.hjson.JsonValue
import plp.common.CONFIG_FILENAME
import java.io.File

@Serializable
data class AwsConfig(val bucket: Bucket)

@Serializable
private data class TopLevelConfig(
    @SerialName("transcribe_aws") val awsConfig: AwsConfig
)

var AWS_CONFIG_FILENAME = CONFIG_FILENAME

private fun getConfig(): AwsConfig {
    val configFile = File(AWS_CONFIG_FILENAME)
    if (!configFile.exists()) {
        throw RuntimeException(
            "missing config file (expected ${configFile.absolutePath}) that should look like this: " +
                """{"bucket": "BUCKET_NAME"}"""
        )
    }
    val configContents = JsonValue.readHjson(configFile.reader()).toString()
    return Json { ignoreUnknownKeys = true }.decodeFromString<TopLevelConfig>(configContents).awsConfig
}

fun getConfiguredBucket(): Bucket {
    return getConfig().bucket
}
