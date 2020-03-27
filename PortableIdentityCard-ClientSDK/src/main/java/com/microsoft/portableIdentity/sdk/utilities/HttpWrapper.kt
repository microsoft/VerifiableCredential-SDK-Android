package com.microsoft.portableIdentity.sdk.utilities

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.content.ByteArrayContent
import io.ktor.http.ContentType

object HttpWrapper {

    private val client = HttpClient(Android)

    suspend fun get(url: String): String {
        return client.get(url)
    }

    suspend fun post(postBody: String, url: String): String? {
        return client.post {
            url(url)
            body = ByteArrayContent(
                bytes = stringToByteArray(postBody),
                contentType = ContentType.Application.FormUrlEncoded)
        }
    }
}