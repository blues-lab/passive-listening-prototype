package plp.hub.prerecording

import plp.logging.KotlinLogging
import uk.co.labbookpages.WavFile
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.nameWithoutExtension

private val logger = KotlinLogging.logger { }

/**
 * Return this file's duration in seconds
 */
val WavFile.duration: Long
    get() {
        return this.numFrames / this.sampleRate
    }

/**
 * Split the given WAV file into segments of the given length
 * If the input file is named recording.wav, the outputs will named recording_01.wav, recording_02.wav, etc.
 *
 * @param file the file to split
 * @param targetDirectory where to put the resulting files
 * @param segmentLengthSeconds the duration of each segment, in seconds
 * @return a list of the segment files written
 */
fun splitFile(file: Path, targetDirectory: Path, segmentLengthSeconds: Int): List<Path> {
    require(file.exists())
    require(targetDirectory.isDirectory())
    require(segmentLengthSeconds > 0)

    logger.debug { "splitting $file" }

    val wavFile = WavFile.openWavFile(file.toFile())
    logger.debug { "recording duration is ${wavFile.duration} seconds" }

    val framesPerSegment = segmentLengthSeconds * wavFile.sampleRate

    var segmentCount = 0
    var framesLeftToCopy = wavFile.numFrames

    val totalSegmentCount = framesLeftToCopy / framesPerSegment
    logger.debug { "will create $totalSegmentCount segments" }

    val segmentPaths: MutableList<Path> = ArrayList(totalSegmentCount.toInt())

    while (framesLeftToCopy > 0) {
        val numFramesToCopy = minOf(framesPerSegment, framesLeftToCopy)

        val segmentFileName = file.nameWithoutExtension + "_%02d".format(segmentCount) + ".wav"
        val segmentFilePath = targetDirectory.resolve(segmentFileName)

        logger.debug { "writing segment $segmentCount of $totalSegmentCount to $segmentFilePath" }

        val segmentFile = WavFile.newWavFile(
            segmentFilePath.toFile(),
            wavFile.numChannels,
            numFramesToCopy,
            wavFile.validBits,
            wavFile.sampleRate
        )

        val copyBuffer = IntArray(numFramesToCopy.toInt())
        wavFile.readFrames(copyBuffer, copyBuffer.size)
        segmentFile.writeFrames(copyBuffer, copyBuffer.size)
        segmentFile.close()

        segmentPaths.add(segmentFilePath)

        framesLeftToCopy -= numFramesToCopy
        segmentCount++
    }

    return segmentPaths
}
