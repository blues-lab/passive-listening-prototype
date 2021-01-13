package plp.hub

enum class RecordingStatus {
    ACTIVE,
    PAUSED,
    CANCELED,
}

object RecordingState {
    var status = RecordingStatus.ACTIVE
}
