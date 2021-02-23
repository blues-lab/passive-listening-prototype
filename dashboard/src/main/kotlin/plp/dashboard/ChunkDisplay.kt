package plp.dashboard

import react.RProps
import react.dom.a
import react.dom.td
import react.dom.tr
import react.functionalComponent
import kotlin.js.Date

external interface ChunkDisplayProps: RProps {
    var chunk: Chunk
}

val chunkDisplay = functionalComponent<ChunkDisplayProps> {  props ->
    val chunk = props.chunk
    val date = Date(chunk.timestamp * 1000)

    tr {
        td { date.toLocaleString() }
        td {
            a(href="/audio/${chunk.filename}") {
                +"audio"
            }
        }
        td { chunk.text }
        td { chunk.classifier }
        td { chunk.classification }
        td { chunk.extras }
    }
}