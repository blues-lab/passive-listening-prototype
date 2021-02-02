package plp.transcribe

import plp.common.runCommandAndGetOutput
import plp.logging.KotlinLogging
import java.io.File
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

/**
 * Transcribe audio using wav2letter
 */

private val logger = KotlinLogging.logger {}

/** Number of wav2letter jobs that have been launched */
val totalJobCount = AtomicInteger(0)

/**
 * Extract the text from a line of wav2letter output
 */
internal fun extractTextFromWav2letterLine(line: String): String? {
    val regex = Regex("^\\d+,\\d+,(.*)")
    val match = regex.find(line) ?: return null
    return match.groupValues[1]
}

/**
 * Extract just the text from a complete output of the wav2letter program
 */
internal fun extractTextFromWav2letterOutput(output: String): String {
    val lines = output.lines()
    return lines.mapNotNull { extractTextFromWav2letterLine(it) }.joinToString("\n").trim()
}

/**
 * Transcribe the given file using wav2letter and return the transcription
 *
 * @param fileToTranscribe the file to transcribe
 * @param modelDir the directory that contains the model files needed by wav2letter
 */
fun transcribeFile(fileToTranscribe: File, modelDir: File): String {
    val transcriptionDir = fileToTranscribe.parent
    val filename = fileToTranscribe.name
    val modelPath = modelDir.absolutePath
    logger.info { "transcribing $filename using model at $modelPath" }
    val jobId = totalJobCount.getAndIncrement()
    val command = listOf(
        "docker",
        "run",
        "--rm",
        "-v",
        "$modelPath:/data/model",
        "-v",
        "$transcriptionDir:/data/audio",
        // "-it",
        "--ipc=host",
        "--name",
        "wav2letter_$jobId",
        "-a",
        "stdin",
        "-a",
        "stdout",
        "-a",
        "stderr",
        "wav2letter/wav2letter:inference-latest",
        "sh",
        "-c",
        "cat /data/audio/$filename | /root/wav2letter/build/inference/inference/examples/simple_streaming_asr_example --input_files_base_path /data/model",
    )
    val output = runCommandAndGetOutput(command)
    val transcript = extractTextFromWav2letterOutput(output)
    logger.debug { "transcribed $filename as $transcript" }
    return transcript
}

class Wav2letterTranscriber(private val modelDir: Path) : Transcriber {
    override fun transcribeFile(file: Path): String {
        return transcribeFile(file.toFile(), modelDir.toFile())
    }
}
