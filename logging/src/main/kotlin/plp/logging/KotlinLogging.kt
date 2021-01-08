package plp.logging

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Given a closure, infer the file/class name from it.
 *
 * This code is adapted from the kotlin-logging library,
 * which is licensed under the Apache 2.0 license and is Copyright (c) 2016-2018 Ohad Shai.
 */
private fun classNameFromClosure(func: () -> Unit): String {
    val name = func.javaClass.name
    return when {
        name.contains("Kt$") -> name.substringBefore("Kt$")
        name.contains("$") -> name.substringBefore("$")
        else -> name
    }
}

/**
 * Returns the current time, formatted, as a string
 */
private fun getCurrentTime(): String {
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    return current.format(formatter)
}

/**
 * This module attempts to implement a standalone logger
 * that is *somewhat* API-compatible with KotlinLogging API of the kotlin-logging library:
 * https://github.com/MicroUtils/kotlin-logging/blob/master/LICENSE
 */
object KotlinLogging {
    fun logger(name: String): Logger {
        return Logger(name)
    }

    fun logger(func: () -> Unit): Logger {
        return logger(classNameFromClosure(func))
    }
}

enum class LogLevel { DEBUG, INFO, WARNING, ERROR }

class Logger(private val name: String) {
    fun debug(msg: String?) {
        debug { msg }
    }

    fun debug(msg: () -> Any?) {
        log(LogLevel.DEBUG, msg)
    }

    fun info(msg: String?) {
        info { msg }
    }

    fun info(msg: () -> Any?) {
        log(LogLevel.INFO, msg)
    }

    fun warning(msg: String?) {
        warning { msg }
    }

    fun warning(msg: () -> Any?) {
        log(LogLevel.WARNING, msg)
    }

    fun error(msg: String?) {
        error { msg }
    }

    fun error(msg: () -> Any?) {
        log(LogLevel.ERROR, msg)
    }

    private fun log(level: LogLevel, msg: () -> Any?) {
        val messageString = msg.invoke().toString()
        val statement = "${getCurrentTime()} - $name - $level - $messageString"
        System.err.println(statement)
    }
}
