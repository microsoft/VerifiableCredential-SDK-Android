// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.utilities.controlflow

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed class Return<S, F> {
    class Success<S, F>(val payload: S) : Return<S, F>()
    class Failure<S, F>(val payload: F) : Return<S, F>()
}

interface AwaitCallback<S, F> {
    fun onSuccess(payload: S)
    fun onFailure(payload: F)
}

suspend fun <S, F> awaitCallback(block: (AwaitCallback<S, F>) -> Unit) : Return<S, F> =
    suspendCoroutine { cont ->
        block(object : AwaitCallback<S, F> {
            override fun onSuccess(payload: S) = cont.resume(Return.Success(payload))
            override fun onFailure(payload: F) = cont.resume(Return.Failure(payload))
        })
    }