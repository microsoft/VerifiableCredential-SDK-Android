// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import com.microsoft.did.sdk.util.MetricsConstants.DURATION
import com.microsoft.did.sdk.util.MetricsConstants.NAME
import com.microsoft.did.sdk.util.log.SdkLog
import retrofit2.Response

object MetricsConstants {
    const val NAME = "eventName"
    const val DURATION = "duration_ms"
}

inline fun <R> logTime(name: String, block: () -> R): R {
    val start = System.currentTimeMillis()
    val result = block()
    val elapsedTime = System.currentTimeMillis() - start
    SdkLog.event(
        "DIDPerformanceMetrics", mapOf(
            NAME to name,
            DURATION to "$elapsedTime"
        )
    )
    return result
}

inline fun <S> logNetworkTime(name: String, block: () -> Response<S>): Response<S> {
    val start = System.currentTimeMillis()
    val result = block()
    val elapsedTime = System.currentTimeMillis() - start

    val cvRequest = result.raw().request.headers[Constants.CORRELATION_VECTOR_HEADER] ?: "none"
    val cvResponse = result.raw().headers[Constants.CORRELATION_VECTOR_HEADER] ?: "none"
    val requestId = result.raw().headers[Constants.REQUEST_ID_HEADER] ?: "none"

    SdkLog.event(
        "DIDNetworkMetrics", mapOf(
            NAME to name,
            DURATION to "$elapsedTime",
            "CV_request" to cvRequest,
            "CV_response" to cvResponse,
            "request_Id" to requestId,
            "isSuccessful" to "${result.isSuccessful}",
            "code" to "${result.code()}"
        )
    )
    return result
}
