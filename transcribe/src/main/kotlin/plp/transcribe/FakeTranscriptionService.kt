package plp.transcribe

import Transcription
import TranscriptionServiceGrpcKt
import plp.logging.KotlinLogging

private val logger = KotlinLogging.logger {}

class FakeTranscriptionService : TranscriptionServiceGrpcKt.TranscriptionServiceCoroutineImplBase() {
    init {
        logger.info("using fake transcription service")
    }

    override suspend fun transcribeFile(request: Transcription.TranscriptionRequest): Transcription.TranscriptionResponse {
        logger.info { "handling request ${request.id}" }
        return Transcription.TranscriptionResponse.newBuilder().setId(1).setText("THIS IS A TEST TRANSCRIPTION").build()
    }
}
