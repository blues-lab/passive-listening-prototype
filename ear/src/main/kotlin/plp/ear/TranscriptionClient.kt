package plp.ear

import Transcription
import TranscriptionServiceGrpcKt
import com.google.protobuf.ByteString
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import plp.common.rpc.MutualAuthInfo
import plp.logging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.inputStream

const val TRANSCRIPTION_SERVICE_HOST = "localhost"
const val TRANSCRIPTION_SERVICE_PORT = plp.common.rpc.DEFAULT_PORT

private val logger = KotlinLogging.logger {}

@ExperimentalPathApi
class MutualAuthTranscriptionClient(mutualAuthInfo: MutualAuthInfo) :
    Transcriber {
    private val stub: TranscriptionServiceGrpcKt.TranscriptionServiceCoroutineStub

    init {
        val channel = NettyChannelBuilder.forAddress(TRANSCRIPTION_SERVICE_HOST, TRANSCRIPTION_SERVICE_PORT)
            .useTransportSecurity()
            .sslContext(mutualAuthInfo.sslContext)
            .build()

        stub = TranscriptionServiceGrpcKt.TranscriptionServiceCoroutineStub(channel)
    }

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
