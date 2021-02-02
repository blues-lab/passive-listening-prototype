package plp.transcribe

import plp.logging.KotlinLogging
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

class FakeTranscriber : Transcriber {
    init {
        logger.info("using fake transcription service")
    }

    override suspend fun transcribeFile(file: Path): String {
        return "THIS IS A TEST TRANSCRIPTION"
    }
}
