package plp.common

import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

/**
 * Replace ~ in given string with user's home directory
 */
fun resolveHomeDirectory(path: String): String {
    return path.replaceFirst("~", System.getProperty("user.home"))
}

/**
 * Replace ~ in given string with user's home directory
 */
@ExperimentalPathApi
fun String.toPath(): Path {
    return Path(resolveHomeDirectory(this))
}
