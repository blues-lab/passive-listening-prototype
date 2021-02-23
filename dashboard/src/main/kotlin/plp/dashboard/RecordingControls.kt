package plp.dashboard

import kotlinx.browser.window
import react.RProps
import react.child
import react.functionalComponent
import react.useEffectWithCleanup
import react.useState
import kotlin.js.Promise

const val REFRESH_INTERVAL_SECONDS = 2

fun getStatus(): Promise<RecordingStatus> {
    val url = "$BASE_URL/recording/status"
    return window.fetch(url).then { response -> response.text() }.then { text ->
        when (text) {
            "ACTIVE" -> RecordingStatus.ACTIVE
            "PAUSING" -> RecordingStatus.PAUSING
            "PAUSED" -> RecordingStatus.PAUSED
            "CANCELING" -> RecordingStatus.CANCELING
            "CANCELED" -> RecordingStatus.CANCELED
            else -> RecordingStatus.UNKNOWN
        }
    }
}

val recordingControls = functionalComponent<RProps> {
    val (status, setStatus) = useState(RecordingStatus.UNKNOWN)
    useEffectWithCleanup {
        getStatus().then { newStatus -> setStatus(newStatus) }

        val intervalId = window.setInterval({
            getStatus().then { newStatus -> setStatus(newStatus) }
        }, REFRESH_INTERVAL_SECONDS * 1000)

        fun() { window.clearInterval(intervalId) }
    }

    child(recordingControlButton) {
        attrs.status = status
    }
}