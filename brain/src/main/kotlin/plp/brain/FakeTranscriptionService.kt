package plp.brain

import plp.logging.KotlinLogging

private val logger = KotlinLogging.logger {}

class FakeTranscriptionService : TranscriptionServiceGrpcKt.TranscriptionServiceCoroutineImplBase() {
    override suspend fun transcribeFile(request: Transcription.TranscriptionRequest): Transcription.TranscriptionResponse {
        logger.info { "handling request ${request.id}" }
        return Transcription.TranscriptionResponse.newBuilder().setId(1).setText("hello world").build()
    }
}
