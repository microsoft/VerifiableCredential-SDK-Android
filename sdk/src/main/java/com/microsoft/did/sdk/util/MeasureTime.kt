// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import com.microsoft.did.sdk.util.MetricsConstants.NAME
import com.microsoft.did.sdk.util.MetricsConstants.TIME
import com.microsoft.did.sdk.util.log.SdkLog
import retrofit2.Response

object MetricsConstants {
    const val NAME = "Name"
    const val TIME = "Time"
}

inline fun <R> logTime(name: String, block: () -> R): R {
    val start = System.currentTimeMillis()
    val result = block()
    val elapsedTime = System.currentTimeMillis() - start
    SdkLog.event(
        "PerformanceMetrics", mapOf(
            NAME to name,
            TIME to "$elapsedTime"
        )
    )
    return result
}

inline fun <S> logNetworkTime(name: String, block: () -> Response<S>): Response<S> {
    val start = System.currentTimeMillis()
    val result = block()
    val elapsedTime = System.currentTimeMillis() - start

    val cvRequest = result.raw().request().headers()[Constants.CORRELATION_VECTOR_HEADER] ?: "none"
    val cvResponse = result.raw().headers()[Constants.CORRELATION_VECTOR_HEADER] ?: "none"

    SdkLog.event(
        "NetworkMetrics", mapOf(
            NAME to name,
            TIME to "$elapsedTime",
            "CV_request" to cvRequest,
            "CV_response" to cvResponse,
            "isSuccessful" to "${result.isSuccessful}",
            "code" to "${result.code()}"
        )
    )
    return result
}
