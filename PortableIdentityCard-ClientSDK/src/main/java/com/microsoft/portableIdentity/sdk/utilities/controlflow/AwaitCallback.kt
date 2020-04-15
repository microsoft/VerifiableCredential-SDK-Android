// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.utilities.controlflow

import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

interface AwaitResultCallback<S, F> {
    fun onSuccess(payload: S)
    fun onFailure(payload: F)
}

suspend fun <S, F> awaitResultCallback(block: (AwaitResultCallback<S, F>) -> Unit) : Result<S, F> =
    suspendCoroutine { cont ->
        block(object : AwaitResultCallback<S, F> {
            override fun onSuccess(payload: S) = cont.resume(Result.Success(payload))
            override fun onFailure(payload: F) = cont.resume(Result.Failure(payload))
        })
    }