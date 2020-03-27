package com.microsoft.portableIdentity.sdk.utilities

import java.lang.RuntimeException
import java.util.regex.Pattern

object SdkLog {
    interface Consumer {
        fun log(logLevel: Level, message: String, tag: String)
    }

    enum class Level {
        VERBOSE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FAILURE;

        fun severity() = ordinal
    }

    private const val ORIGINAL_CALLER_STACK_INDEX = 2
    private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")
    private val consumers: MutableList<Consumer> = ArrayList()

    fun addConsumer(consumer: Consumer) = consumers.add(consumer)

    fun v(message: String, tag: String = implicitTag()) {
        log(Level.VERBOSE, message, tag)
    }

    fun d(message: String, tag: String = implicitTag()) {
        log(Level.DEBUG, message, tag)
    }

    fun i(message: String, tag: String = implicitTag()) {
        log(Level.INFO, message, tag)
    }

    fun w(message: String, tag: String = implicitTag()) {
        log(Level.WARN, message, tag)
    }

    fun e(message: String, tag: String = implicitTag()) {
        log(Level.ERROR, message, tag)
    }

    fun f(message: String, tag: String = implicitTag()) {
        log(Level.FAILURE, message, tag)
    }

    private fun log(logLevel: Level, message: String, tag: String) {
        consumers.forEach { it.log(logLevel, message, tag) }
    }

    @Deprecated("Use short form.", ReplaceWith("this.d(message, tag)"))
    fun debug(message: String, tag: String = implicitTag()) = d(message, tag)

    @Deprecated(
        "Legacy error log function that returns an Exception. Remove all references, then delete this method.",
        ReplaceWith("this.e(message, tag)")
    )
    fun error(message: String, tag: String = implicitTag()): RuntimeException {
        log(Level.ERROR, message, tag)
        return RuntimeException(message)
    }

    private fun implicitTag(): String {
        val stackElement = getCallerElement(Throwable().stackTrace, ORIGINAL_CALLER_STACK_INDEX)
        return if (stackElement != null) createStackElementTag(stackElement) else "INVALID_TAG"
    }

    @Suppress("SameParameterValue")
    private fun getCallerElement(stackTrace: Array<StackTraceElement>, index: Int): StackTraceElement? {
        if (stackTrace.size <= index) {
            w("Synthetic stacktrace didn't have enough elements: are you using proguard?", "SdkLog.implicitTag")
            return null
        }
        // Calls from Java into Kotlin's @JvmStatic methods have an extra anonymous method. We have to skip it.
        if (stackTrace[index].className == this.javaClass.name
            && stackTrace[index].methodName.length == 1
            && stackTrace.size > index + 1
        ) {
            return stackTrace[index + 1]
        }
        return stackTrace[index]
    }

    private fun createStackElementTag(element: StackTraceElement): String {
        val matcher = ANONYMOUS_CLASS.matcher(element.className)
        val tag = if (matcher.find()) matcher.replaceAll("") + "\$ANON\$" else element.className
        return tag.substring(tag.lastIndexOf('.') + 1) + "." + element.methodName
    }
}