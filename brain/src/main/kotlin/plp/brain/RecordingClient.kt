package plp.brain

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import plp.common.configureLogging
import plp.proto.AudioRecordingGrpcKt
import plp.proto.AudioRecordingOuterClass
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.writeBytes

const val DEFAULT_PORT = 50058
const val RECORDING_SEGMENT_DURATION_SECONDS = 5

@ExperimentalPathApi
fun main(args: Array<String>) = runBlocking {
    configureLogging()

    val parser = ArgParser("RecordingServer")
    val key by parser.option(ArgType.String).required()
    val cert by parser.option(ArgType.String).required()
    val root by parser.option(ArgType.String).required()
    val tmpDir by parser.option(ArgType.String).required()
    parser.parse(args)

    val tmpPath = Path(tmpDir)

    val logger = KotlinLogging.logger {}
    logger.info("starting Recording client")

    val sslContext = GrpcSslContexts.forClient()
        .trustManager(File(root))
        .keyManager(File(cert), File(key))
        .build()
    val channel = NettyChannelBuilder.forAddress("localhost", DEFAULT_PORT).useTransportSecurity()
        .sslContext(sslContext)
        .build()

    val stub = AudioRecordingGrpcKt.AudioRecordingCoroutineStub(channel)

    val recordingSpec = AudioRecordingOuterClass.RecordingSessionSpecification.newBuilder().setSegmentDuration(
        RECORDING_SEGMENT_DURATION_SECONDS
    ).build()
    val recordings = stub.streamRecordings(recordingSpec)

    recordings.collect { recording: AudioRecordingOuterClass.Recording ->
        logger.info { "received recording from ${recording.timestamp}" }
        val filename = "${recording.timestamp}.wav"
        val filePath = tmpPath / filename
        filePath.writeBytes(recording.audio.toByteArray())
        logger.debug { "wrote ${recording.audio.size()} bytes to $filePath" }
    }
}
