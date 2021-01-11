package plp.brain

import java.nio.file.Path

fun interface Transcriber {
    fun transcribeFile(file: Path): String
}
