package plp.hub

import plp.data.Database
import plp.hub.web.DashboardClientList
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

enum class RecordingStatus {
    ACTIVE,
    PAUSING,
    PAUSED,
    CANCELING,
    CANCELED,
}

object RecordingState {
    var status = RecordingStatus.PAUSED
    var database: Database? = null

    @ExperimentalPathApi
    var audioFileDirectory = Path("/tmp")
}

object DashboardSate {
    var dashboardListeners: DashboardClientList? = null
}
