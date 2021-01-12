package plp.brain

import plp.logging.KotlinLogging
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.writeBytes
import java.nio.file.Path as NioFilePath

private val logger = KotlinLogging.logger {}

/**
 * Transcribe given audio
 *
 * @param modelPath path to wav2letter's model files
 * @param tmpDir location for temporary files. Can't use the system default because tmpfs isn't mounted correctly into Docker containers, producing empty transcriptions
 */
class TranscriptionService(private val modelPath: NioFilePath, private val tmpDir: NioFilePath) :
    TranscriptionServiceGrpcKt.TranscriptionServiceCoroutineImplBase() {
    @ExperimentalPathApi
    @Suppress("TooGenericExceptionCaught")
    override suspend fun transcribeFile(request: Transcription.TranscriptionRequest): Transcription.TranscriptionResponse {
        logger.info { "handling request ${request.id}" }

        return try {
            val tempFile = createTempFile(directory = tmpDir, suffix = ".wav")
            logger.debug { "storing request ${request.id} bytes in $tempFile" }
            tempFile.writeBytes(request.audio.toByteArray())

            val text = transcribeFile(tempFile.toFile(), modelPath.toFile())

            logger.debug { "cleaning up $tempFile now that transcription is done" }
            tempFile.deleteExisting()

            Transcription.TranscriptionResponse.newBuilder().setId(request.id).setText(text).build()
        } catch (e: Exception) {
            logger.error { e }
            throw e
        }
    }
}
