package plp.transcribe.aws

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AwsTranscribeTest {
    @Test
    fun `parses the transcript text from the result JSON`() {
        assertEquals(
            " that's no answer",
            getTranscriptFromAwsJSON(
                """
               {
                  "jobName":"job ID",
                  "accountId":"account ID",
                  "results": {
                     "transcripts":[
                        {
                           "transcript":" that's no answer"
                        }
                     ],
                     "items":[
                        {
                           "start_time":"0.180",
                           "end_time":"0.470",
                           "alternatives":[
                              {
                                 "confidence":0.84,
                                 "word":"that's"
                              }
                           ]
                        },
                        {
                           "start_time":"0.470",
                           "end_time":"0.710",
                           "alternatives":[
                              {
                                 "confidence":0.99,
                                 "word":"no"
                              }
                           ]
                        },
                        {
                           "start_time":"0.710",
                           "end_time":"1.080",
                           "alternatives":[
                              {
                                 "confidence":0.87,
                                 "word":"answer"
                              }
                           ]
                        }
                     ]
                  },
                  "status":"COMPLETED"
               }
                """.trimIndent()
            )
        )
    }
}
