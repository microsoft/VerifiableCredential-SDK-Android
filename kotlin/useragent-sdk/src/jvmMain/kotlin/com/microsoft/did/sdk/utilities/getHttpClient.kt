package com.microsoft.did.sdk.utilities

import io.ktor.client.HttpClient

actual fun getHttpClient(): HttpClient {
    println("using jvm compiled method")
    return HttpClient()
}