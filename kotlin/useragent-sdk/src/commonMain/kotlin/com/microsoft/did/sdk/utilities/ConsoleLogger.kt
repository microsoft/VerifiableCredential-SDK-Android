package com.microsoft.did.sdk.utilities

class ConsoleLogger(var debug: Boolean = false): ILogger {

    override fun log(message: String) {
        println(message)
    }

    override fun warn(message: String) {
        println("[WARN]: $message")
    }

    override fun debug(message: String) {
        if (debug) {
            println("[WARN]: $message")
        }
    }

    override fun error(message: String) {
        println("[ERROR]: $message")
    }

    override fun fatal(message: String) {
        println("[FATAL]: $message")
    }
}