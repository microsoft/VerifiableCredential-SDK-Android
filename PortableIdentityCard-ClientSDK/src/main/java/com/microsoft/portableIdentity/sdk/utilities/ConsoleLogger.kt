package com.microsoft.portableIdentity.sdk.utilities

class ConsoleLogger(var debug: Boolean = false): ILogger {

    override fun log(message: String) {
        println(message)
    }

    override fun warn(message: String) {
        println("[WARN]: $message")
    }

    override fun debug(message: String) {
        if (debug) {
            println("[DEBUG]: $message")
        }
    }

    override fun error(message: String): Error {
        println("[ERROR]: $message")
        throw Error(message)
    }

    override fun fatal(message: String): Error {
        println("[FATAL]: $message")
        throw Error(message)
    }
}