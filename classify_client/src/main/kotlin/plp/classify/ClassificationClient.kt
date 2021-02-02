package plp.classify

import plp.common.Service
import plp.common.currentUnixTime
import plp.common.rpc.client.GrpcChannelChoice
import plp.logging.KotlinLogging
import plp.proto.Classification
import plp.proto.ClassificationServiceGrpcKt

private val logger = KotlinLogging.logger {}

open class ClassificationClient(grpcChannelChoice: GrpcChannelChoice, service: Service) {
    private val stub: ClassificationServiceGrpcKt.ClassificationServiceCoroutineStub =
        ClassificationServiceGrpcKt.ClassificationServiceCoroutineStub(
            grpcChannelChoice.makeChannel(
                service.host,
                service.port,
            )
        )

    suspend fun classifyText(text: String): Classification.ClassificationResponse {
        val timestamp = currentUnixTime()
        logger.debug { "$timestamp requesting classification of $text" }
        val request = Classification.ClassificationRequest.newBuilder().setId(timestamp).setText(text).build()
        val response = stub.classifyText(request)
        logger.info { "${currentUnixTime()} received response $response" }
        return response
    }
}
