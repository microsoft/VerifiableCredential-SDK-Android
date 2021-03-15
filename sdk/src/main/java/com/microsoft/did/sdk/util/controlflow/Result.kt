/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.util.controlflow

import com.microsoft.did.sdk.util.log.SdkLog
import kotlinx.coroutines.CancellationException

typealias Success = Boolean

sealed class Result<out S> {
    class Success<out S>(val payload: S) : Result<S>()
    class Failure(val payload: SdkException) : Result<Nothing>()
}

fun <U, T> Result<T>.map(transform: (T) -> U): Result<U> =
    when (this) {
        is Result.Success -> Result.Success(transform(payload))
        is Result.Failure -> this
    }

fun <T> Result<T>.mapError(transform: (SdkException) -> SdkException): Result<T> =
    when (this) {
        is Result.Success -> this
        is Result.Failure -> Result.Failure(transform(payload))
    }

fun <U, T> Result<T>.andThen(transform: (T) -> Result<U>): Result<U> =
    when (this) {
        is Result.Success -> transform(payload)
        is Result.Failure -> this
    }

suspend fun <T> runResultTry(block: suspend RunResultTryContext.() -> Result<T>): Result<T> =
    try {
        RunResultTryContext().block()
    } catch (ex: RunResultTryAbortion) {
        Result.Failure(ex.error as SdkException)
    } catch (ex: CancellationException) {
        throw ex
    } catch (ex: SdkException) {
        SdkLog.w("Internal Sdk Exception", ex)
        Result.Failure(ex)
    } catch (ex: Exception) {
        SdkLog.e("Unhandled Sdk Exception", ex)
        Result.Failure(SdkException("Unhandled Exception", ex))
    }

class RunResultTryContext {
    fun <T> Result<T>.abortOnError(): T =
        when (this) {
            is Result.Success -> payload
            is Result.Failure -> throw RunResultTryAbortion(payload as Any)
        }
}

private class RunResultTryAbortion(val error: Any) : SdkException()