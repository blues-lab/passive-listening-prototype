package plp.hub.vad

import com.google.protobuf.ByteString
import plp.common.GLOBAL_CONFIG
import plp.common.Transcriber
import plp.common.rpc.client.GrpcChannelChoice
import plp.logging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.inputStream
import kotlin.io.path.readBytes

private val logger = KotlinLogging.logger {}

/**
 * This is a client for the Vad service, which determines whether or not an audio clip has speech in it.
 */
@ExperimentalPathApi
class ChromeTranscriber(grpcChannelChoice: GrpcChannelChoice) : Transcriber{
    private val stub: TranscriptionServiceGrpcKt.TranscriptionServiceCoroutineStub =
        TranscriptionServiceGrpcKt.TranscriptionServiceCoroutineStub(
            grpcChannelChoice.makeChannel(
                GLOBAL_CONFIG.vadService.host,
                GLOBAL_CONFIG.vadService.port,
            )
        )

    override suspend fun transcribeFile(file: Path): String {
        logger.debug { "checking $file for speech" }

        val fileContents = ByteString.copyFrom(file.readBytes())
        val request = Transcription.TranscriptionRequest.newBuilder().setAudio(fileContents).build()
        val response = stub.transcribeFile(request)
        val text = response.text
        logger.debug("Transcription result: " + text)
        return text
    }
}
