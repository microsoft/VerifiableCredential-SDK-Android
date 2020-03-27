package com.microsoft.portableIdentity.sdk.utilities

import android.util.Log

class AndroidLogCatConsumer: SdkLog.Consumer {

    override fun log(logLevel: SdkLog.Level, message: String, tag: String) {
        Log.println(getAndroidLogLevel(logLevel), tag, message)
    }

    private fun getAndroidLogLevel(logLevel: SdkLog.Level): Int {
        return logLevel.severity() + 2
    }
}