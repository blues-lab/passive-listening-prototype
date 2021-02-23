package plp.dashboard

import kotlinx.browser.document
import react.*
import react.dom.*

fun main() {
    render(document.getElementById("root")) {
        child(welcome) {
            attrs.name = "world!"
        }

        child(recordingControls) {}

        child(scrollControls) {}
    }
}

external interface WelcomeProps : RProps {
    var name: String
}

private val welcome = functionalComponent<WelcomeProps> { props ->
    h1 {
        +"Hello, ${props.name}"
    }
}


