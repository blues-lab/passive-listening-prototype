package plp.transcribe

import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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
private fun transcribeFile(fileToTranscribe: File, modelDir: File): String {
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

/** Regex representing silence in the transcript */
private val SILENCE_REGEX = Regex("""[h\s]+""")

/**
 * Given a transcript, return an empty string if the transcript is inferred to just cover silence.
 * Otherwise, return the original transcript.
 *
 * This is needed because if wav2letter gets audio without any talking,
 * it will return a non-empty transcript that looks like "h h h h h"
 */
fun filterTranscribedSilence(transcript: String): String {
    return if (SILENCE_REGEX matches transcript) {
        logger.trace { "based on heuristics, transcript is just silence" }
        ""
    } else {
        transcript
    }
}

/**
 * @param modelDir path to directory that contains wav2letter model files. It will be mapped into the Docker container.
 * @param maxParallelJobs Maximum number of wav2letter jobs to allow concurrently
 */
class Wav2letterTranscriber(
    private val modelDir: Path,
    maxParallelJobs: Int = 1
) : Transcriber {

    private val semaphore = Semaphore(maxParallelJobs)

    override suspend fun transcribeFile(file: Path): String {
        logger.trace { "will start transcription, as soon as semaphore allows" }
        semaphore.withPermit { // ensures that only MAX_JOB_COUNT jobs are happening at once
            logger.trace { "semaphore allowed, starting transcription" }
            return filterTranscribedSilence(transcribeFile(file.toFile(), modelDir.toFile()))
        }
    }
}
