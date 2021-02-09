package plp.transcribe.aws

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class S3Test {
    @Test
    fun `extracts bucket from S3 URL`() {
        assertEquals(
            "nm-transcriptions",
            S3Location("https://s3.us-west-2.amazonaws.com/nm-transcriptions/job_sample.wav.json").bucket
        )
    }

    @Test
    fun `extracts filename from S3 URL`() {
        assertEquals(
            "job_sample.wav.json",
            S3Location("https://s3.us-west-2.amazonaws.com/nm-transcriptions/job_sample.wav.json").key
        )
    }
}
