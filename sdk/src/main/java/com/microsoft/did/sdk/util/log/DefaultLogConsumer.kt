// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util.log

import android.util.Log

class DefaultLogConsumer : SdkLog.Consumer {

    override fun log(logLevel: SdkLog.Level, message: String, throwable: Throwable?, tag: String) {
        if (throwable == null) {
            Log.println(getAndroidLogLevel(logLevel), tag, message)
        } else {
            Log.println(getAndroidLogLevel(logLevel), tag, message + "\n" + Log.getStackTraceString(throwable))
        }
    }

    private fun getAndroidLogLevel(logLevel: SdkLog.Level): Int {
        return logLevel.severity() + 2
    }
}