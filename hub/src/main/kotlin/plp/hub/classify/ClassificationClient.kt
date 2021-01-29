package plp.hub.classify

import plp.common.Service
import plp.common.rpc.GrpcChannelChoice
import plp.hub.TranscribedRecording
import plp.logging.KotlinLogging
import plp.proto.Classification
import plp.proto.ClassificationServiceGrpcKt

private val logger = KotlinLogging.logger {}

class ClassificationClient(grpcChannelChoice: GrpcChannelChoice, service: Service) {
    private val stub: ClassificationServiceGrpcKt.ClassificationServiceCoroutineStub =
        ClassificationServiceGrpcKt.ClassificationServiceCoroutineStub(
            grpcChannelChoice.makeChannel(
                service.host,
                service.port,
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

typealias ClassificationClientList = List<ClassificationClient>
