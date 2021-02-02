package plp.transcribe

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

class Wav2letterTest {
    @Test
    fun `extracting text from a single line of wav2letter output returns the text`() {
        assertEquals("we are out", extractTextFromWav2letterLine("2000,3000,we are out"))
        assertEquals("some fruits", extractTextFromWav2letterLine("15000,16000,some fruits"))
    }

    @Test
    fun `extracting text from line without text returns empty string`() {
        assertEquals("", extractTextFromWav2letterLine("0,1000,"))
        assertEquals("", extractTextFromWav2letterLine("13000,14000,"))
        assertEquals("", extractTextFromWav2letterLine("20000,20000,"))
    }

    @Test
    fun `lines without number at the front throw exceptions`() {
        assertNull(extractTextFromWav2letterLine("...2000,3000,we are out"))
        assertNull(extractTextFromWav2letterLine(""))
    }

    @Test
    fun `extracts text from real output`() {
        val commandOutput =
            """
        Started features model file loading ...
        Completed features model file loading elapsed time=22856 microseconds

        Started acoustic model file loading ...
        Completed acoustic model file loading elapsed time=4086 milliseconds

        Started tokens file loading ...
        Completed tokens file loading elapsed time=5468 microseconds

        Tokens loaded - 9998 tokens
        Started decoder options file loading ...
        Completed decoder options file loading elapsed time=1741 microseconds

        Started create decoder ...
        [Letters] 9998 tokens loaded.
        [Words] 200001 words loaded.
        Completed create decoder elapsed time=6568 milliseconds

        Started converting audio input from stdin to text... ...
        Creating LexiconDecoder instance.
        #start (msec), end(msec), transcription
        0,1000,
        1000,2000,
        2000,3000,we are out
        3000,4000,out of
        4000,5000,to paper he tells from pan
        5000,6000,
        6000,7000,i finished
        7000,8000,the box this morning
        8000,9000,i ate
        9000,10000,all trips last night
        10000,11000,when you are washing will be
        11000,12000,it
        12000,13000,looks like we're almost on a figure
        13000,14000,
        14000,15000,i need your and the story and get
        15000,16000,some fruits
        16000,17000,i just think the last
        17000,18000,apple
        18000,19000,we should get more milk
        19000,20000,said in a drinking
        20000,20000,
        Completed converting audio input from stdin to text... elapsed time=4502 milliseconds
            """.trimIndent()
        val expected =
            """
        we are out
        out of
        to paper he tells from pan

        i finished
        the box this morning
        i ate
        all trips last night
        when you are washing will be
        it
        looks like we're almost on a figure

        i need your and the story and get
        some fruits
        i just think the last
        apple
        we should get more milk
        said in a drinking
            """.trimIndent()

        assertEquals(expected, extractTextFromWav2letterOutput(commandOutput))
    }

    @Test
    fun `filter doesn't change valid transcripts`() {
        assertEquals("hello world", filterTranscribedSilence("hello world"))
    }

    @Test
    fun `filters out transcripts that are just h`() {
        assertEquals("", filterTranscribedSilence("h h h h h h h"))
    }

    @Test
    fun `filters out transcripts that have h with different whitespace`() {
        assertEquals("", filterTranscribedSilence("h h h        h h h h"))
        assertEquals("", filterTranscribedSilence("h                      h"))
        assertEquals("", filterTranscribedSilence("               h                      h    "))
        assertEquals(
            "",
            filterTranscribedSilence(
                """               h      
            
                           h 
                             h
                              h
                                h 
                                """
            )
        )
    }
}
