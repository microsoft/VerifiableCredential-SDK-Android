// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.di

import android.content.Context
import com.microsoft.did.sdk.CorrelationVectorService
import com.microsoft.did.sdk.util.Constants.CORRELATION_VECTOR_HEADER
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class CorrelationVectorInterceptor @Inject constructor(
    private val correlationVectorService: CorrelationVectorService,
    private val context: Context
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Chain): Response {
        val originalRequest = chain.request()
        val correlationVector = correlationVectorService.incrementAndSave(context)
        val requestWithCorrelationVectorBuilder = originalRequest.newBuilder()
        if (correlationVector.isNotEmpty())
            requestWithCorrelationVectorBuilder.header(CORRELATION_VECTOR_HEADER, correlationVector)
        val requestWithCorrelationVector = requestWithCorrelationVectorBuilder.build()
        return chain.proceed(requestWithCorrelationVector)
    }
}