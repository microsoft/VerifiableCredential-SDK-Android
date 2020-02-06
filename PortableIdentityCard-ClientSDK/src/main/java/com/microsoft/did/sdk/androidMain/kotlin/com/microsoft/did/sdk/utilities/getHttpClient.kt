package com.microsoft.did.sdk.utilities

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

actual fun getHttpClient(): HttpClient {
     println("IN Android Main")
     return HttpClient(Android) {}
}