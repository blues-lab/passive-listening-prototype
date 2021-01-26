package plp.hub

import plp.data.Database
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

enum class RecordingStatus {
    ACTIVE,
    PAUSED,
    CANCELED,
}

object RecordingState {
    var status = RecordingStatus.ACTIVE
    var database: Database? = null

    @ExperimentalPathApi
    var audioFileDirectory = Path("/tmp")
}
