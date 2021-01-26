package plp.hub.transcription

import Transcription
import TranscriptionServiceGrpcKt
import com.google.protobuf.ByteString
import plp.common.TRANSCRIPTION_SERVICE_HOST
import plp.common.TRANSCRIPTION_SERVICE_PORT
import plp.common.rpc.GrpcChannelChoice
import plp.hub.Recording
import plp.hub.getTimestampFromRecording
import plp.logging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.inputStream

private val logger = KotlinLogging.logger {}

@ExperimentalPathApi
open class TranscriptionClient(grpcChannelChoice: GrpcChannelChoice) :
    Transcriber {
    private val stub: TranscriptionServiceGrpcKt.TranscriptionServiceCoroutineStub =
        TranscriptionServiceGrpcKt.TranscriptionServiceCoroutineStub(
            grpcChannelChoice.makeChannel(
                TRANSCRIPTION_SERVICE_HOST,
                TRANSCRIPTION_SERVICE_PORT
            )
        )

    override suspend fun transcribeFile(file: Path): String {
        logger.debug { "requesting transcription of $file" }

        val fileContents = ByteString.readFrom(file.inputStream())
        val fileId =
            getTimestampFromRecording(Recording(file)) // TODO: recreating a Recording is hacky, should pass in ID directly or at least infer it without the new boxing

        val transcriptionRequest =
            Transcription.TranscriptionRequest.newBuilder().setAudio(fileContents).setId(fileId).build()
        val response = stub.transcribeFile(transcriptionRequest)
        return response.text
    }
}
