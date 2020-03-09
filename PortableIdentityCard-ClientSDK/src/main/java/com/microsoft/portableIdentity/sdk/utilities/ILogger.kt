package com.microsoft.portableIdentity.sdk.utilities

interface ILogger {
    /**
     * Always Logged
     */
    fun log(message: String)

    /**
     * Always logged with warning annotation
     */
    fun warn(message: String)

    /**
     * Logged if in debug mode
     */
    fun debug(message: String)

    /**
     * Always logged with error annotation
     */
    fun error(message: String): Error

    /**
     * Always logged with fatal error annotation
     */
    fun fatal(message: String): Error
}