package plp.common

/**
 * Replace ~ in given string with user's home directory
 */
fun resolveHomeDirectory(path: String): String {
    return path.replaceFirst("~", System.getProperty("user.home"))
}
