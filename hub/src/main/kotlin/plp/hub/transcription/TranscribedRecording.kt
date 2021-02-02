package plp.hub.transcription

import plp.hub.database.RegisteredRecording

open class TranscribedRecording(recording: RegisteredRecording, val transcription: String, val transcriptId: Long) :
    RegisteredRecording(recording, recording.id) {

    /**
     * This boolean represents whether this recording's transcription has any usable text.
     */
    val usableTranscription = transcription != ""

    override fun fieldsToString(): String {
        return super.fieldsToString() + ", transcription=$transcription"
    }
}
