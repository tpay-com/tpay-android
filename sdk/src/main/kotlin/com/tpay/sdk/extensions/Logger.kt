@file:Suppress("unused")

package com.tpay.sdk.extensions

import android.os.Build
import android.util.Log
import com.tpay.sdk.extensions.Logger.formatKeys
import com.tpay.sdk.extensions.Logger.getClassName
import com.tpay.sdk.extensions.Logger.getLineNumber
import com.tpay.sdk.extensions.Logger.getMethodName
import java.util.*

internal fun <T> d(vararg keys: String, func: Ink.() -> T) = Ink(keys).apply {
    parse(func)
    log(Ink.Level.DEBUG)
}

internal fun <T> i(vararg keys: String, func: Ink.() -> T) = Ink(keys).apply {
    parse(func)
    log(Ink.Level.INFO)
}

internal fun <T> w(vararg keys: String, func: Ink.() -> T) = Ink(keys).apply {
    parse(func)
    log(Ink.Level.WARN)
}

internal fun <T> e(vararg keys: String, func: Ink.() -> T) = Ink(keys).apply {
    parse(func)
    log(Ink.Level.ERROR)
}

internal fun <T> wtf(vararg keys: String, func: Ink.() -> T) = Ink(keys).apply {
    parse(func)
    log(Ink.Level.WTF)
}

internal fun <T> Ink.parse(func: Ink.() -> T) {
    when (val result = func()) {
        is String -> {
            msg(result)
        }
        is Pair<*, *> -> {
            val (msg, error) = result
            msg(msg as String)
            cause(error as Throwable?)
        }
    }
}

internal class Ink(private val keys: Array<out String>) {
    private var msg: String? = null
    private var cause: Throwable? = null

    fun msg(msg: String) {
        this.msg = msg
    }

    fun cause(throwable: Throwable?) {
        this.cause = throwable
    }

    fun log(level: Level = Level.DEBUG): Int {
        val flow = formatKeys(keys)
        val trace = Thread.currentThread().stackTrace
        var message = "(${
            getClassName(
                trace,
                true
            )
        }:${getLineNumber(trace)}) in ${getMethodName(trace)} => $msg [user=${Logger.userId}] $flow"

        cause?.let {
            message += " => cause: ${Log.getStackTraceString(cause)}"
        }

        Logger.customLoggers.onEach { logger ->
            logger.log(message)
            cause?.let { error -> logger.log(error) }
        }

        return if (isloggingEnabled) {
            when (level) {
                Level.DEBUG -> Log.d(getClassName(trace), message)
                Level.INFO -> Log.i(getClassName(trace), message)
                Level.WARN -> Log.w(getClassName(trace), message)
                Level.ERROR -> Log.e(getClassName(trace), message)
                Level.WTF -> Log.wtf(getClassName(trace), message)
            }
        } else {
            -1
        }
    }

    enum class Level { DEBUG, INFO, WARN, ERROR, WTF }
}

internal object Logger {
    interface CustomLogger {
        fun log(msg: String)
        fun log(throwable: Throwable)
    }

    private const val TRACE_INDEX = 4
    var userId: String = "${Build.MANUFACTURER}@${Build.MODEL}"

    val customLoggers: MutableSet<CustomLogger> =
        Collections.synchronizedSet(HashSet())

    fun getMethodName(trace: Array<StackTraceElement>): String =
        getSimpleMethodName(trace[TRACE_INDEX].methodName)

    fun getClassName(trace: Array<StackTraceElement>, full: Boolean = false): String =
        getSimpleClassName(trace[TRACE_INDEX].className, full)

    fun getLineNumber(trace: Array<StackTraceElement>): Int = trace[TRACE_INDEX].lineNumber

    private fun getSimpleClassName(name: String, full: Boolean): String =
        name.substring(name.lastIndexOf(".") + 1) + if (full) ".java" else ""

    private fun getSimpleMethodName(n: String): String {
        var name = n
        val firstIndex = name.indexOf("_")
        name = name.replace("\\$".toRegex(), "->")
        return if (firstIndex > 0) name.substring(0, firstIndex) else name
    }

    fun formatKeys(keys: Array<out String>): String =
        if (keys.isEmpty()) "" else String.format(
            Locale.getDefault(),
            " {keys[%s]}",
            keys.joinToString(" | ")
        )

    /** Custom loggers **/
    fun registerLogger(customLogger: CustomLogger): Boolean =
        if (!customLoggers.contains(customLogger)) customLoggers.add(customLogger) else false

    fun unregisterLogger(customLogger: CustomLogger): Boolean = customLoggers.remove(customLogger)

    fun customLoggersCount(): Int = customLoggers.size
}

internal infix fun String.cause(that: Throwable): Pair<String, Throwable?> = this to that

internal var isloggingEnabled = true
