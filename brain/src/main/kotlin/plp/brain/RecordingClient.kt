package plp.brain

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import plp.common.configureLogging
import plp.common.toPath
import plp.proto.AudioRecordingGrpcKt
import plp.proto.AudioRecordingOuterClass
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.div
import kotlin.io.path.writeBytes

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
        val channel = NettyChannelBuilder.forAddress("localhost", DEFAULT_PORT).useTransportSecurity()
            .sslContext(sslContext)
            .build()

        val stub = AudioRecordingGrpcKt.AudioRecordingCoroutineStub(channel)

        val recordingSpec = AudioRecordingOuterClass.RecordingSessionSpecification.newBuilder().setSegmentDuration(
            RECORDING_SEGMENT_DURATION_SECONDS
        ).build()
        return stub.streamRecordings(recordingSpec)
    }
}

@ExperimentalPathApi
fun handleRecordings(tmpPath: Path, recordings: Flow<AudioRecordingOuterClass.Recording>) = runBlocking {
    recordings.collect { recording: AudioRecordingOuterClass.Recording ->
        logger.info { "received recording from ${recording.timestamp}" }
        val filename = "${recording.timestamp}.wav"
        val filePath = tmpPath / filename
        filePath.writeBytes(recording.audio.toByteArray())
        logger.debug { "wrote ${recording.audio.size()} bytes to $filePath" }
    }
}

@ExperimentalPathApi
fun main(args: Array<String>) = runBlocking {
    configureLogging()

    val parser = ArgParser("RecordingServer")
    val key by parser.option(ArgType.String).required()
    val cert by parser.option(ArgType.String).required()
    val root by parser.option(ArgType.String).required()
    val tmpDir by parser.option(ArgType.String).required()
    parser.parse(args)

    val client = MutualAuthRecordingClient(key = key.toPath(), cert = cert.toPath(), root = root.toPath())
    handleRecordings(tmpDir.toPath(), client.receiveRecordings())
}
