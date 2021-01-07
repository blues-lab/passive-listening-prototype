package plp.brain

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import plp.data.Database
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
fun handleRecordings(audioDirectory: Path, database: Database, recordings: Flow<AudioRecordingOuterClass.Recording>) =
    runBlocking {
        val queries = database.audioQueries

        recordings.collect { recording: AudioRecordingOuterClass.Recording ->
            logger.info { "received recording from ${recording.timestamp}" }

            // Write audio to file
            val filename = "${recording.timestamp}.wav"
            val filePath = audioDirectory / filename
            filePath.writeBytes(recording.audio.toByteArray())
            logger.debug { "wrote ${recording.audio.size()} bytes to $filePath" }

            // Save recording to database
            queries.insert(
                filename,
                recording.timestamp.toLong(),
                RECORDING_SEGMENT_DURATION_SECONDS.toDouble()
            ) // FIXME: use computed recording duration
        }
    }
