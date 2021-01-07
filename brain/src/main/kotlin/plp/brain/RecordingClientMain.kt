package plp.brain

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.coroutines.runBlocking
import plp.common.configureLogging
import plp.common.toPath
import plp.data.Database
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@ExperimentalPathApi
fun main(args: Array<String>) = runBlocking {
    configureLogging()

    val parser = ArgParser("RecordingServer")
    val key by parser.option(ArgType.String).required()
    val cert by parser.option(ArgType.String).required()
    val root by parser.option(ArgType.String).required()
    val dataDir by parser.option(ArgType.String).required()
    parser.parse(args)

    val dataPath = dataDir.toPath()
    val audioPath = dataPath / "audio"
    if (!audioPath.exists()) {
        audioPath.createDirectories()
    } else if (!audioPath.isDirectory()) {
        throw IllegalArgumentException("audio directory exists and isn't a directory: $audioPath")
    }

    val dbPath = dataPath / "db.sqlite"
    val dbConnection = "jdbc:sqlite:$dbPath"
    val driver: SqlDriver = JdbcSqliteDriver(dbConnection)
    val database = Database(driver)

    val client = MutualAuthRecordingClient(key = key.toPath(), cert = cert.toPath(), root = root.toPath())
    handleRecordings(dataDir.toPath(), database, client.receiveRecordings())
}
