package plp.hub.database

import plp.hub.recording.Recording

open class RegisteredRecording(recording: Recording, val id: Long) : Recording(recording.path) {
    override fun fieldsToString(): String {
        return super.fieldsToString() + ", id=$id"
    }
}
