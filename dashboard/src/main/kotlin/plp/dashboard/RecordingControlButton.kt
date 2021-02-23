package plp.dashboard

import kotlinx.html.ButtonType
import kotlinx.html.js.onClickFunction
import react.Fragment
import react.RProps
import react.dom.button
import react.functionalComponent

external interface RecordingControlButtonProps : RProps {
    var status: RecordingStatus
}

val recordingControlButton = functionalComponent<RecordingControlButtonProps> { props ->
    when (props.status) {
        RecordingStatus.ACTIVE -> button(type = ButtonType.button) {
            attrs.onClickFunction = fun(event) { console.log("clicked pause") }
            +"Pause"
        }
        RecordingStatus.PAUSED -> button(type = ButtonType.button)
        {
            attrs.onClickFunction = fun(event) { console.log("clicked start") }
            +"Start"
        }
        RecordingStatus.UNKNOWN -> button(type = ButtonType.button)
        {
            attrs.onClickFunction = fun(event) { console.log("clicked ???") }
            +"???"
        }
        else -> Fragment
    }
}