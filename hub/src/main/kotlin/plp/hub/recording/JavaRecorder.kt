@file:Suppress("MagicNumber")

package plp.hub.recording

import plp.logging.KotlinLogging
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine
import java.nio.file.Path as JavaPath

/* Standard recording settings */
val fileType: AudioFileFormat.Type = AudioFileFormat.Type.WAVE
val audioFormat: AudioFormat = run {
    val sampleRate = 16000f
    val sampleSizeInBits = 16
    val channels = 1
    val signed = true
    val bigEndian = true
    AudioFormat(
        sampleRate,
        sampleSizeInBits,
        channels,
        signed,
        bigEndian
    )
}

/** An exception thrown when something in the recording process goes wrong */
class AudioRecordingException(override val message: String?) : RuntimeException(message)

private val logger = KotlinLogging.logger {}

/**
 * Class that uses the Java Audio API to make a single recording
 *
 * Based on the tutorial at
 * https://www.codejava.net/coding/capture-and-record-sound-into-wav-file-with-java-sound-api
 */
class AudioRecorder(
    private val outputPath: JavaPath
) {
    private var line: TargetDataLine? = null

    /**
     * Start capturing sound and saving it to a file
     */
    fun start() {
        val info = DataLine.Info(TargetDataLine::class.java, audioFormat)

        if (!AudioSystem.isLineSupported(info)) {
            throw AudioRecordingException("Audio format not supported: $audioFormat")
        }

        line = AudioSystem.getLine(info) as TargetDataLine
        line!!.open(audioFormat)
        line!!.start()
        val stream = AudioInputStream(line)

        logger.debug { "starting recording" }
        AudioSystem.write(stream, fileType, outputPath.toFile())
    }

    /**
     * Close the target data line to finish capturing and recording
     */
    fun stop() {
        line!!.stop()
        line!!.close()
        logger.debug("stopped recording")
    }
}

val RecordJava = RecordOnce { durationSeconds, path ->
    val recorder = AudioRecorder(path)
    val durationMilliseconds: Long = (durationSeconds * 1000).toLong()

    val stopper = Thread {
        Thread.sleep(durationMilliseconds)
        recorder.stop()
    }
    stopper.start()

    recorder.start()
}
