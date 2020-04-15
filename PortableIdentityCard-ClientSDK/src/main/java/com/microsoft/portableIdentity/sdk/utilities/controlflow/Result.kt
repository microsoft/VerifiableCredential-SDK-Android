/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.utilities.controlflow

sealed class Result<out S, out F: PortableIdentitySdkException> {
    class Success<out S>(val payload: S) : Result<S, Nothing>()
    class Failure(val payload: PortableIdentitySdkException) : Result<Nothing, PortableIdentitySdkException>()
}

fun <U, T> Result<T, PortableIdentitySdkException>.map(transform: (T) -> U): Result<U, PortableIdentitySdkException> =
    when (this) {
        is Result.Success -> Result.Success(transform(payload))
        is Result.Failure -> this
    }

fun <T> Result<T, PortableIdentitySdkException>.mapError(transform: (PortableIdentitySdkException) -> PortableIdentitySdkException): Result<T, PortableIdentitySdkException> =
    when (this) {
        is Result.Success -> this
        is Result.Failure -> Result.Failure(transform(payload))
    }

fun <U, T> Result<T, PortableIdentitySdkException>.andThen(transform: (T) -> Result<U, PortableIdentitySdkException>): Result<U, PortableIdentitySdkException> =
    when (this) {
        is Result.Success -> transform(payload)
        is Result.Failure -> this
    }

suspend fun <T, E> runResultTry(block: suspend RunResultTryContext<E>.() -> Result<T, PortableIdentitySdkException>): Result<T, PortableIdentitySdkException> =
    try {
        RunResultTryContext<E>().block()
    } catch (ex: RunResultTryAbortion) {
        Result.Failure(ex.error as PortableIdentitySdkException)
    }

class RunResultTryContext<E> {
    fun <T> Result<T, PortableIdentitySdkException>.abortOnError(): T =
        when (this) {
            is Result.Success -> payload
            is Result.Failure -> throw RunResultTryAbortion(payload as Any)
        }
}

private class RunResultTryAbortion(val error: Any) : PortableIdentitySdkException()