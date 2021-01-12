package plp.hub

import java.nio.file.Path

interface Transcriber {
    suspend fun transcribeFile(file: Path): String
}
