package plp.hub.classify

import plp.common.GLOBAL_CONFIG
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
                GLOBAL_CONFIG.classificationServices[0].host,
                GLOBAL_CONFIG.classificationServices[0].port,
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
