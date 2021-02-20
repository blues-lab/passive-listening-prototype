package plp.common

enum class OS {
    UNKNOWN, WINDOWS, LINUX, MAC, SOLARIS
}

/**
 * Get the current system's OS
 * via https://stackoverflow.com/a/31547504
 */
fun getOS(): OS {
    val os = System.getProperty("os.name").toLowerCase()
    return when {
        os.contains("win") -> {
            OS.WINDOWS
        }
        os.contains("nix") || os.contains("nux") || os.contains("aix") -> {
            OS.LINUX
        }
        os.contains("mac") -> {
            OS.MAC
        }
        os.contains("sunos") -> {
            OS.SOLARIS
        }
        else -> OS.UNKNOWN
    }
}
