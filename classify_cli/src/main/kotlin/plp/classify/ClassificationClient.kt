package plp.classify

import plp.common.SAMPLE_CLASSIFICATION_SERVICE_HOST
import plp.common.SAMPLE_CLASSIFICATION_SERVICE_PORT
import plp.common.currentUnixTime
import plp.common.rpc.GrpcChannelChoice
import plp.logging.KotlinLogging
import plp.proto.Classification
import plp.proto.ClassificationServiceGrpcKt
import kotlin.io.path.ExperimentalPathApi

private val logger = KotlinLogging.logger {}

@ExperimentalPathApi
open class ClassificationClient(grpcChannelChoice: GrpcChannelChoice) {
    private val stub: ClassificationServiceGrpcKt.ClassificationServiceCoroutineStub =
        ClassificationServiceGrpcKt.ClassificationServiceCoroutineStub(
            grpcChannelChoice.makeChannel(
                SAMPLE_CLASSIFICATION_SERVICE_HOST,
                SAMPLE_CLASSIFICATION_SERVICE_PORT
            )
        )

    suspend fun classifyText(text: String) {
        val timestamp = currentUnixTime()
        logger.debug { "$timestamp requesting classification of $text" }
        val request = Classification.ClassificationRequest.newBuilder().setId(timestamp).setText(text).build()
        val response = stub.classifyText(request)
        logger.info { "${currentUnixTime()} received response $response" }
    }
}
