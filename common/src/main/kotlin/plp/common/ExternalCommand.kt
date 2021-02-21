package plp.common

import plp.logging.KotlinLogging
import java.util.concurrent.TimeUnit

/**
 * How long to wait (in seconds) before giving up on the external command
 * NOTE: waitFor is preempted by waiting from the program's output, so this is not necessarily the limit for how long the external command will run. (That may be indefinite!)
 */
const val TIMEOUT_SECONDS = 45L

private val logger = KotlinLogging.logger {}

open class ExternalCommandError(message: String) : RuntimeException(message)
class ExternalCommandTimeout(message: String) : ExternalCommandError(message)

/**
 * Run the given external command and return its output as a string
 *
 * @throws ExternalCommandTimeout if the timeout of TIMEOUT_SECONDS has been reached
 * @throws ExternalCommandError if the program returns with a non-zero exit code
 */
fun runCommandAndGetOutput(commandAndArguments: List<String>): String {
    val processBuilder = ProcessBuilder(commandAndArguments)
        .redirectErrorStream(true)
    logger.debug { "running command $commandAndArguments" }
    logger.debug { commandAndArguments.joinToString(" ") }
    val process = processBuilder.start()
    val output = process.inputStream.bufferedReader().readText().trim()
    val timedOut = !process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
    if (timedOut) {
        val errorMessage = "command timed out after $TIMEOUT_SECONDS seconds with output: $output"
        logger.error(errorMessage)
        throw ExternalCommandTimeout(errorMessage)
    }

    logger.trace { "finished running command" }

    // Check return code and throw on non-standard exit values
    if (process.exitValue() != 0) {
        val errorMessage = "command exited with code ${process.exitValue()} and output: $output"
        logger.error(errorMessage)
        throw ExternalCommandError(errorMessage)
    }

    logger.debug { "command output: $output" }

    return output
}
