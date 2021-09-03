package plp.hub.web

import plp.common.Service
import plp.common.rpc.client.GrpcChannelChoice
import plp.hub.classify.ClassificationClient
import plp.hub.transcription.TranscribedRecording
import plp.logging.KotlinLogging
private val logger = KotlinLogging.logger {}

class DashboardClient(grpcChannelChoice: GrpcChannelChoice, service: Service) {
    private val stub: DashboardServiceGrpcKt.DashboardServiceCoroutineStub =
        DashboardServiceGrpcKt.DashboardServiceCoroutineStub(
            grpcChannelChoice.makeChannel(
                service.host,
                service.port,
            )
        )

    suspend fun queryDashboardData(): Dashboard.DashboardResponse {
        val request = Dashboard.DashboardRequest.newBuilder()
            .setClassificationLimit(1)
            .setDashboardResultType("raw").build()
        val response = stub.getDashboardData(request)
        response.textList

        logger.debug { "recieved $response" }
        dashboardData[response.classificationName] = response.textList.joinToString(separator = "\n")
        return response
    }
}

typealias DashboardClientList = List<DashboardClient>
