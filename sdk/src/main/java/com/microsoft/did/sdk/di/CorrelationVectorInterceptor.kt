// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.di

import android.content.Context
import android.preference.PreferenceManager
import com.microsoft.correlationvector.CorrelationVector
import com.microsoft.did.sdk.util.log.SdkLog
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class CorrelationVectorInterceptor @Inject constructor(private val context: Context) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Chain): Response {
        val originalRequest = chain.request()
        val correlationVectorString = readCorrelationVector(context)
        val requestWithCorrelationVectorBuilder = originalRequest.newBuilder()
        var debugCv: String? = null
        if (correlationVectorString != null) {
            val correlationVectorIncremented = CorrelationVector.parse(correlationVectorString).increment()
            writeCorrelationVector(context, correlationVectorIncremented)
            requestWithCorrelationVectorBuilder.header("ms-cv", correlationVectorIncremented)
            debugCv = correlationVectorIncremented
        }
        val requestWithCorrelationVector = requestWithCorrelationVectorBuilder.build()
        SdkLog.d("Making network call ${requestWithCorrelationVector.url()} with correlation vector $debugCv")
        return chain.proceed(requestWithCorrelationVector)
    }

    private fun writeCorrelationVector(applicationContext: Context, correlationId: String) {
        PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().putString("correlation_vector", correlationId).apply()
    }

    private fun readCorrelationVector(applicationContext: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("correlation_vector", null)
    }
}