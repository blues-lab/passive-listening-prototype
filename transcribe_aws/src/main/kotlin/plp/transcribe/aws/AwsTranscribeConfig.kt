package plp.transcribe.aws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import plp.common.CONFIG_FILENAME
import plp.common.loadConfigJson

@Serializable
data class AwsConfig(val bucket: Bucket)

@Serializable
private data class TopLevelConfig(
    @SerialName("transcribe_aws") val awsConfig: AwsConfig
)

var AWS_CONFIG_FILENAME = CONFIG_FILENAME

private fun getConfig(): AwsConfig {
    val configContents = loadConfigJson(AWS_CONFIG_FILENAME)
        ?: throw RuntimeException(
            "missing config file $AWS_CONFIG_FILENAME that should look like this: " +
                """{"transcribe_aws": {"bucket": "BUCKET_NAME"}}"""
        )
    return Json { ignoreUnknownKeys = true }.decodeFromString<TopLevelConfig>(configContents).awsConfig
}

fun getConfiguredBucket(): Bucket {
    return getConfig().bucket
}
