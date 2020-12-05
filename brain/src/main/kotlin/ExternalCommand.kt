import mu.KotlinLogging
import java.util.concurrent.TimeUnit

/**
 * How long to wait (in seconds) before giving up on the external command
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
    logger.debug { "running command $commandAndArguments" }
    logger.debug { commandAndArguments.joinToString(" ") }
    val process = processBuilder.start()

    val timedOut = !process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
    if (timedOut) {
        val stdOut = process.inputStream.bufferedReader().readText()
        val errorMessage = "command timed out after $TIMEOUT_SECONDS seconds with output: $stdOut"
        logger.error(errorMessage)
        throw ExternalCommandTimeout(errorMessage)
    }

    // Check return code and throw on non-standard exit values
    if (process.exitValue() != 0) {
        val stdOut = process.inputStream.bufferedReader().readText().trim()
        val stdErr = process.errorStream.bufferedReader().readText().trim()
        var errorInfo = ""
        if (stdOut.isNotEmpty()) errorInfo += "<STDOUT> $stdOut \n"
        if (stdErr.isNotEmpty()) errorInfo += "<STDERR> $stdErr \n"
        logger.error(errorInfo)
        throw ExternalCommandError(errorInfo)
    }

    val stdOut = process.inputStream.bufferedReader().readText()
    logger.debug { "command output: $stdOut" }

    return stdOut
}
