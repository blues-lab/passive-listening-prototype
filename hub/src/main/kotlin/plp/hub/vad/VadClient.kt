package plp.hub.vad

import Vad
import VadServiceGrpcKt
import com.google.protobuf.ByteString
import plp.common.GLOBAL_CONFIG
import plp.common.rpc.client.GrpcChannelChoice
import plp.hub.database.RegisteredRecording
import plp.logging.KotlinLogging
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.inputStream

private val logger = KotlinLogging.logger {}

/**
 * This is a client for the Vad service, which determines whether or not an audio clip has speech in it.
 */
@ExperimentalPathApi
class VadClient(grpcChannelChoice: GrpcChannelChoice) {
    private val stub: VadServiceGrpcKt.VadServiceCoroutineStub =
        VadServiceGrpcKt.VadServiceCoroutineStub(
            grpcChannelChoice.makeChannel(
                GLOBAL_CONFIG.vadService.host,
                GLOBAL_CONFIG.vadService.port,
            )
        )

    suspend fun recordingHasSpeech(recording: RegisteredRecording): Boolean {
        logger.debug { "checking $recording for speech" }

        val fileContents = ByteString.readFrom(recording.path.inputStream())

        val request = Vad.VadRequest.newBuilder().setId(recording.id.toInt()).setAudio(fileContents).build()
        val response = stub.checkAudioForSpeech(request)
        logger.debug {
            "vad responded, saying recording ${request.id} is ${
            if (response.isSpeech) {
                ""
            } else {
                "NOT"
            }
            } speech"
        }
        return response.isSpeech
    }
}
