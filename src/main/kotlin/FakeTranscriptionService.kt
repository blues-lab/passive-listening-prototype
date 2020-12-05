import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class FakeTranscriptionService : TranscriptionServiceGrpcKt.TranscriptionServiceCoroutineImplBase() {
    override suspend fun transcribeFile(request: Stt.TranscriptionRequest): Stt.TranscriptionResponse {
        logger.info { "handling request ${request.id}" }
        return Stt.TranscriptionResponse.newBuilder().setId(1).setText("hello world").build()
    }
}
