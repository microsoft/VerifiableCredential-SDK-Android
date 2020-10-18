// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.di

import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import java.io.IOException

class UserAgentInterceptor(private val userAgentInfo: String) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Chain): Response {
        val originalRequest = chain.request()
        val requestWithUserAgentInfo = originalRequest.newBuilder()
            .header("User-Agent", userAgentInfo)
            .build()
        return chain.proceed(requestWithUserAgentInfo)
    }
}