package plp.common

import java.nio.file.Path

interface Transcriber {
    suspend fun transcribeFile(file: Path): String
}
