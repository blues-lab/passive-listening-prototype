package plp.transcribe.aws

import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import plp.common.Transcriber
import plp.logging.KotlinLogging
import software.amazon.awssdk.services.transcribe.TranscribeClient
import software.amazon.awssdk.services.transcribe.model.GetTranscriptionJobRequest
import software.amazon.awssdk.services.transcribe.model.LanguageCode
import software.amazon.awssdk.services.transcribe.model.Media
import software.amazon.awssdk.services.transcribe.model.MediaFormat
import software.amazon.awssdk.services.transcribe.model.StartTranscriptionJobRequest
import software.amazon.awssdk.services.transcribe.model.TranscriptionJob
import software.amazon.awssdk.services.transcribe.model.TranscriptionJobStatus
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.name

private val logger = KotlinLogging.logger {}

class TranscriptionException(override val message: String?) : RuntimeException(message)

/** When waiting for a transcription, sleep for this long before checking again */
const val STATUS_CHECK_INTERVAL_MILLISECONDS = 5000L

fun getTranscriptFromAwsJSON(responseContent: String): String? {
    val json = Json.parseToJsonElement(responseContent)
    val transcript = json.jsonObject["results"]
        ?.jsonObject?.get("transcripts")
        ?.jsonArray?.firstOrNull()
        ?.jsonObject?.get("transcript")
        ?.jsonPrimitive?.contentOrNull

    if (transcript == null) {
        logger.error { "transcript not found in AWS JSON: $responseContent" }
    }

    return transcript
}

class AwsTranscribe(private val s3Client: S3) : Transcriber {
    private val awsClient: TranscribeClient = TranscribeClient.builder().region(REGION).build()

    @ExperimentalPathApi
    override suspend fun transcribeFile(file: Path): String {
        val filename = file.name
        val fileUrl = s3Client.bucket.pathToFile(filename)

        s3Client.uploadFile(file.toFile())

        val jobName = filename
        val request = StartTranscriptionJobRequest.builder()
            .transcriptionJobName(jobName)
            .mediaFormat(MediaFormat.WAV)
            .languageCode(LanguageCode.EN_US)
            .media(Media.builder().mediaFileUri(fileUrl).build())
            .outputBucketName(s3Client.bucket)
            .build()

        logger.debug { "initiating transcription request for $fileUrl" }
        var job: TranscriptionJob = awsClient.startTranscriptionJob(request).transcriptionJob()

        while (true) {
            // Check job status
            when (val status: TranscriptionJobStatus = job.transcriptionJobStatus()) {
                TranscriptionJobStatus.QUEUED, TranscriptionJobStatus.IN_PROGRESS -> {
                    logger.debug { "transcription job $jobName is still $status, sleeping for $STATUS_CHECK_INTERVAL_MILLISECONDS seconds" }
                    delay(STATUS_CHECK_INTERVAL_MILLISECONDS)

                    logger.trace { "transcription job $jobName is done sleeping, will re-query for status" }
                    val response = awsClient.getTranscriptionJob(
                        GetTranscriptionJobRequest.builder()
                            .transcriptionJobName(jobName)
                            .build()
                    )
                    job = response.transcriptionJob()
                }

                TranscriptionJobStatus.COMPLETED -> {
                    logger.debug { "transcription job $jobName is done" }

                    break
                }

                TranscriptionJobStatus.FAILED, TranscriptionJobStatus.UNKNOWN_TO_SDK_VERSION -> {
                    logger.error("transcription job $jobName failed: ${job.failureReason()}")
                    throw TranscriptionException("transcription failed: ${job.failureReason()}")
                }
            }
        }

        logger.debug { "since transcription job $jobName is finished, cleaning up recording file" }
        s3Client.deleteFile(file.name)

        val resultUrl = job.transcript().transcriptFileUri()
        val resultFilename = S3Location(resultUrl).key
        logger.debug { "retrieving transcription job $jobName result from $resultUrl ($resultFilename in default bucket)" }
        val resultFile = s3Client.getFile(resultFilename)
        val transcript = getTranscriptFromAwsJSON(resultFile)
            ?: throw TranscriptionException("transcription failed: no transcript found in output")

        logger.info { "transcribed $filename as `$transcript`" }

        logger.debug { "cleaning up transcription output" }
        s3Client.deleteFile(resultFilename)

        return transcript
    }
}
