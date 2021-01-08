package plp.brain

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import kotlinx.coroutines.flow.Flow
import plp.logging.KotlinLogging
import plp.proto.AudioRecordingGrpcKt
import plp.proto.AudioRecordingOuterClass
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi

const val EAR_HOST = "localhost"
const val DEFAULT_PORT = 50058
const val RECORDING_SEGMENT_DURATION_SECONDS = 5

private val logger = KotlinLogging.logger {}

fun interface RecordingClient {
    fun receiveRecordings(): Flow<AudioRecordingOuterClass.Recording>
}

@ExperimentalPathApi
class MutualAuthRecordingClient(private val root: Path, private val cert: Path, private val key: Path) :
    RecordingClient {
    override fun receiveRecordings(): Flow<AudioRecordingOuterClass.Recording> {
        logger.info("starting Recording client")

        val sslContext = GrpcSslContexts.forClient()
            .trustManager(root.toFile())
            .keyManager(cert.toFile(), key.toFile())
            .build()
        val channel = NettyChannelBuilder.forAddress(EAR_HOST, DEFAULT_PORT).useTransportSecurity()
            .sslContext(sslContext)
            .build()

        val stub = AudioRecordingGrpcKt.AudioRecordingCoroutineStub(channel)

        val recordingSpec = AudioRecordingOuterClass.RecordingSessionSpecification.newBuilder().setSegmentDuration(
            RECORDING_SEGMENT_DURATION_SECONDS
        ).build()
        return stub.streamRecordings(recordingSpec)
    }
}
