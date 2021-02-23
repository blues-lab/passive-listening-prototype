package plp.dashboard

import kotlinx.browser.window
import kotlinx.html.ButtonType
import kotlinx.html.js.onClickFunction
import kotlinx.html.style
import react.RProps
import react.dom.button
import react.dom.div
import react.functionalComponent

const val SCROLL_STEP = 100.0

val scrollControls = functionalComponent<RProps> {
    div {
        attrs {
            style =
                kotlinext.js.js {
                    position = "fixed"
                }
        }

        button(type = ButtonType.button) {
            attrs {
                onClickFunction = fun(_) {
                    window.scrollBy(0.0, -SCROLL_STEP)
                }
            }
            +"Scroll up"
        }

        button(type = ButtonType.button) {
            attrs {
                onClickFunction = fun(_) {
                    window.scrollBy(0.0, SCROLL_STEP)
                }
            }
            +"Scroll down"
        }
    }
}