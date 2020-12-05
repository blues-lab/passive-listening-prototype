import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class ExternalCommandTest {
    @Test
    fun `external command returns output string`() {
        assertEquals("Hello, world!\n", runCommandAndGetOutput(listOf("echo", "Hello, world!")))
    }

    @Test
    fun `throws on negative exit codes`() {
        assertFailsWith<ExternalCommandError> { runCommandAndGetOutput(listOf("pwd", "--illegal-flag")) }
    }
}
