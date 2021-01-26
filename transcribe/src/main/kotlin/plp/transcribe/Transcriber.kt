package plp.transcribe

import java.nio.file.Path

fun interface Transcriber {
    fun transcribeFile(file: Path): String
}
