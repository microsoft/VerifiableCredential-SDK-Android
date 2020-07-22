// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util.controlflow

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface AwaitCallback<S> {
    fun onSuccess(payload: S)
    fun onFailure(exception: Exception)
}

suspend fun <S> awaitCallback(block: (AwaitCallback<S>) -> Unit): S =
    suspendCoroutine { cont ->
        block(object : AwaitCallback<S> {
            override fun onSuccess(payload: S) = cont.resume(payload)
            override fun onFailure(exception: Exception) = cont.resumeWithException(exception)
        })
    }

interface AwaitResultCallback<S> {
    fun onSuccess(payload: S)
    fun onFailure(payload: SdkException)
}

suspend fun <S> awaitResultCallback(block: (AwaitResultCallback<S>) -> Unit): Result<S> =
    suspendCoroutine { cont ->
        block(object : AwaitResultCallback<S> {
            override fun onSuccess(payload: S) = cont.resume(Result.Success(payload))
            override fun onFailure(payload: SdkException) = cont.resume(Result.Failure(payload))
        })
    }