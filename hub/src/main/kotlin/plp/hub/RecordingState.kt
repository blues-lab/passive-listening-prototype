package plp.hub

enum class RecordingStatus {
    STOPPED,
    ACTIVE,
}

object RecordingState {
    var status = RecordingStatus.ACTIVE
}
