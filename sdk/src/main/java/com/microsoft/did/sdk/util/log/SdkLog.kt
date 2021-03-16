// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util.log

import java.util.regex.Pattern

object SdkLog {
    interface Consumer {
        fun log(logLevel: Level, message: String, throwable: Throwable? = null, tag: String)

        fun event(name: String, properties: Map<String, String>? = null)
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
    private val CONSUMERS: MutableList<Consumer> = ArrayList()

    fun addConsumer(consumer: Consumer) = CONSUMERS.add(consumer)

    fun event(name: String, properties: Map<String, String>? = null) {
        CONSUMERS.forEach {
            it.event(name, properties)
        }
    }

    fun v(message: String, throwable: Throwable? = null, tag: String = implicitTag()) {
        log(Level.VERBOSE, message, throwable, tag)
    }

    fun d(message: String, throwable: Throwable? = null, tag: String = implicitTag()) {
        log(Level.DEBUG, message, throwable, tag)
    }

    fun i(message: String, throwable: Throwable? = null, tag: String = implicitTag()) {
        log(Level.INFO, message, throwable, tag)
    }

    fun w(message: String, throwable: Throwable? = null, tag: String = implicitTag()) {
        log(Level.WARN, message, throwable, tag)
    }

    fun e(message: String, throwable: Throwable? = null, tag: String = implicitTag()) {
        log(Level.ERROR, message, throwable, tag)
    }

    fun f(message: String, throwable: Throwable? = null, tag: String = implicitTag()) {
        log(Level.FAILURE, message, throwable, tag)
    }

    private fun log(logLevel: Level, message: String, throwable: Throwable? = null, tag: String) {
        CONSUMERS.forEach { it.log(logLevel, message, throwable, tag) }
    }

    private fun implicitTag(): String {
        val stackElement = getCallerElement(
            Throwable().stackTrace,
            ORIGINAL_CALLER_STACK_INDEX
        )
        return if (stackElement != null) createStackElementTag(stackElement) else "INVALID_TAG"
    }

    @Suppress("SameParameterValue")
    private fun getCallerElement(stackTrace: Array<StackTraceElement>, index: Int): StackTraceElement? {
        if (stackTrace.size <= index) {
            w(
                "Synthetic stacktrace didn't have enough elements: are you using proguard?",
                null,
                "SdkLog.implicitTag"
            )
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