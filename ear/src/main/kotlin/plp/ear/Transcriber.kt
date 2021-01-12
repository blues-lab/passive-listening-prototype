package plp.ear

import java.nio.file.Path

interface Transcriber {
    suspend fun transcribeFile(file: Path): String
}
