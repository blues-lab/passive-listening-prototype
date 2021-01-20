package plp.hub

import plp.data.Database

enum class RecordingStatus {
    ACTIVE,
    PAUSED,
    CANCELED,
}

object RecordingState {
    var status = RecordingStatus.ACTIVE
    var database: Database? = null
}
