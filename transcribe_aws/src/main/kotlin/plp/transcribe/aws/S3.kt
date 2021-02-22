package plp.transcribe.aws

import plp.logging.KotlinLogging
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.net.URL

private val logger = KotlinLogging.logger {}

/** An S3 bucket name */
typealias Bucket = String

/** Return the http-style path to the given key (s3://bucket/file) */
fun Bucket.pathToFile(key: String): String {
    return "s3://$this/$key"
}

val REGION: Region = Region.US_WEST_2

/** A location in S3 */
class S3Location(url: String) {
    val bucket: Bucket
    val key: String
    init {
        val javaURL = URL(url)
        val path = javaURL.path
        bucket = path.substring(1) // strip leading '/
            .substringBefore('/')
        key = path.substringAfterLast('/')
    }
}

class S3(val bucket: Bucket) {
    private val s3Client = S3Client.builder().region(REGION).build()

    fun uploadFile(file: File) {
        logger.debug { "uploading $file to S3 bucket $bucket under key ${file.name}" }

        s3Client.putObject(
            PutObjectRequest.builder().bucket(bucket).key(file.name)
                .build(),
            RequestBody.fromFile(file)
        )
    }

    fun deleteFile(filename: String) {
        logger.debug { "deleting $filename from S3 bucket $bucket" }

        val deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucket).key(filename).build()
        s3Client.deleteObject(deleteObjectRequest)
    }

    fun getFile(filename: String): String {
        logger.debug { "retrieving $filename from S3 bucket $bucket" }

        val request = GetObjectRequest.builder().bucket(bucket).key(filename).build()
        val response: ResponseInputStream<GetObjectResponse> = s3Client.getObject(request)

        return response.bufferedReader().use { it.readText() }
    }
}
