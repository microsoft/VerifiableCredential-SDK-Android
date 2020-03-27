package com.microsoft.portableIdentity.sdk.utilities

import android.util.Log

class ConsoleLogger: Logger() {

    override fun log(logLevel: Level, message: String, tag: String) {
        Log.println(getAndroidLogLevel(logLevel), tag, message)
    }

    private fun getAndroidLogLevel(logLevel: Level): Int {
        return logLevel.severity() + 2
    }
}