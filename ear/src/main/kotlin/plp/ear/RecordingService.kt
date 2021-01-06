package plp.ear

import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plp.proto.AudioRecordingGrpcKt
import plp.proto.AudioRecordingOuterClass
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.inputStream
import kotlin.io.path.nameWithoutExtension

@ExperimentalPathApi
internal fun serializeRecording(recording: Recording): AudioRecordingOuterClass.Recording {
    val audioFilePath = recording.path
    val audioBytes = ByteString.readFrom(audioFilePath.inputStream())
    val timestamp = audioFilePath.nameWithoutExtension.toInt()
    return AudioRecordingOuterClass.Recording.newBuilder()
        .setAudio(audioBytes)
        .setTimestamp(timestamp)
        .build()
}

@ExperimentalPathApi
class RecordingService(private val tmpDirectory: Path) : AudioRecordingGrpcKt.AudioRecordingCoroutineImplBase() {
    override fun streamRecordings(request: AudioRecordingOuterClass.RecordingSessionSpecification): Flow<AudioRecordingOuterClass.Recording> {
        val recorder = Recorder(request.segmentDuration, tmpDirectory)
        return recordingFlow(recorder).map(::serializeRecording)
    }
}
