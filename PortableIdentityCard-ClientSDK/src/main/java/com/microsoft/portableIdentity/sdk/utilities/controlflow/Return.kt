// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.utilities.controlflow

import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

sealed class Return<S, F> {
    class Success<S, F>(val payload: S) : Return<S, F>()
    class Failure<S, F>(val payload: F) : Return<S, F>()
}

interface AwaitResultCallback<S, F> {
    fun onSuccess(payload: S)
    fun onFailure(payload: F)
}

suspend fun <S, F> awaitResultCallback(block: (AwaitResultCallback<S, F>) -> Unit) : Return<S, F> =
    suspendCoroutine { cont ->
        block(object : AwaitResultCallback<S, F> {
            override fun onSuccess(payload: S) = cont.resume(Return.Success(payload))
            override fun onFailure(payload: F) = cont.resume(Return.Failure(payload))
        })
    }

interface AwaitCallback<S> {
    fun onSuccess(payload: S)
    fun onFailure(exception: Exception)
}

suspend fun <S> awaitCallback(block: (AwaitCallback<S>) -> Unit) : S =
    suspendCoroutine { cont ->
        block(object : AwaitCallback<S> {
            override fun onSuccess(payload: S) = cont.resume(payload)
            override fun onFailure(exception: Exception) = cont.resumeWithException(exception)
        })
    }