package plp.hub.web

import plp.common.Service
import plp.common.rpc.client.GrpcChannelChoice
import plp.logging.KotlinLogging
import plp.proto.Dashboard
import plp.proto.DashboardServiceGrpcKt

private val logger = KotlinLogging.logger {}

class DashboardClient(grpcChannelChoice: GrpcChannelChoice, service: Service) {
    private val stub: DashboardServiceGrpcKt.DashboardServiceCoroutineStub =
        DashboardServiceGrpcKt.DashboardServiceCoroutineStub(
            grpcChannelChoice.makeChannel(
                service.host,
                service.port,
            )
        )

    suspend fun queryDashboardData(): Map<String, String> {
        val request = Dashboard.DashboardRequest.newBuilder()
            .setClassificationLimit(1)
            .setDashboardResultType("raw").build()
        val response = stub.getDashboardData(request)

        logger.debug { "recieved $response" }
        val dashboardData = emptyMap<String, String>().toMutableMap()
        dashboardData["name"] = response.classificationName
        dashboardData["display_details"] = response.textList.joinToString(separator = "\n")
        return dashboardData
    }
}

typealias DashboardClientList = List<DashboardClient>
