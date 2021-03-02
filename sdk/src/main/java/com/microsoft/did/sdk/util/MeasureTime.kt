// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import com.microsoft.did.sdk.util.log.SdkLog

inline fun <R> logTime(name: String, block: () -> R): R {
    val start = System.currentTimeMillis()
    val result = block()
    val elapsedTime = System.currentTimeMillis() - start
    SdkLog.event("PerformanceMetrics", mapOf("name" to name, "time" to "${elapsedTime}ms"))
    return result
}
