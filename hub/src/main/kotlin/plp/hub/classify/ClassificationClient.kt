package plp.hub.classify

import plp.common.SAMPLE_CLASSIFICATION_SERVICE_HOST
import plp.common.SAMPLE_CLASSIFICATION_SERVICE_PORT
import plp.common.rpc.GrpcChannelChoice
import plp.hub.TranscribedRecording
import plp.logging.KotlinLogging
import plp.proto.Classification
import plp.proto.ClassificationServiceGrpcKt
import kotlin.io.path.ExperimentalPathApi

private val logger = KotlinLogging.logger {}

@ExperimentalPathApi
class ClassificationClient(grpcChannelChoice: GrpcChannelChoice) {
    private val stub: ClassificationServiceGrpcKt.ClassificationServiceCoroutineStub =
        ClassificationServiceGrpcKt.ClassificationServiceCoroutineStub(
            grpcChannelChoice.makeChannel(
                SAMPLE_CLASSIFICATION_SERVICE_HOST,
                SAMPLE_CLASSIFICATION_SERVICE_PORT
            )
        )

    suspend fun classifyRecording(recording: TranscribedRecording): Classification.ClassificationResponse {
        logger.debug { "requesting classification of $recording" }
        val request = Classification.ClassificationRequest.newBuilder().setId(recording.id.toInt())
            .setText(recording.transcription).build()
        val response = stub.classifyText(request)
        logger.debug { "classified $recording as $response" }
        return response
    }
}
