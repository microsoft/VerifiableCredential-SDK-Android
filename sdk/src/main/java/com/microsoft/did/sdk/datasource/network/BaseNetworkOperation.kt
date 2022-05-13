/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network

import com.microsoft.did.sdk.util.Constants.CORRELATION_VECTOR_HEADER
import com.microsoft.did.sdk.util.Constants.REQUEST_ID_HEADER
import com.microsoft.did.sdk.util.NetworkErrorParser
import com.microsoft.did.sdk.util.controlflow.ClientException
import com.microsoft.did.sdk.util.controlflow.ForbiddenException
import com.microsoft.did.sdk.util.controlflow.LocalNetworkException
import com.microsoft.did.sdk.util.controlflow.NetworkException
import com.microsoft.did.sdk.util.controlflow.NotFoundException
import com.microsoft.did.sdk.util.controlflow.RedirectException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.ServiceUnreachableException
import com.microsoft.did.sdk.util.controlflow.UnauthorizedException
import com.microsoft.did.sdk.util.log.SdkLog
import com.microsoft.did.sdk.util.logNetworkTime
import retrofit2.Response
import java.io.IOException

/**
 * Base Network Operation class with default methods for all Network Operations.
 * S: The Response Body Type from the Service.
 * T: The Object transformed from the response body.
 * In default methods, S == T, for no transformation takes place.
 * fire method will just return Result.Success(responseBody: S)
 */
abstract class BaseNetworkOperation<S, T> {

    abstract val call: suspend () -> Response<S>

    open suspend fun fire(): Result<T> {
        try {
            val response = logNetworkTime("${this::class.simpleName}") {
                call.invoke()
            }
            if (response.isSuccessful) {
                return onSuccess(response)
            }
            return onFailure(response)
        } catch (exception: IOException) {
            return Result.Failure(LocalNetworkException("Failed to send request.", exception))
        }
    }

    open suspend fun onSuccess(response: Response<S>): Result<T> {
        // TODO("how do we want to handle null bodies")
        // TODO("how to not suppress this warning")
        @Suppress("UNCHECKED_CAST")
        val transformedPayload = (response.body() ?: throw LocalNetworkException("Body of Response is null.")) as T
        return Result.Success(transformedPayload)
    }

    // TODO("what do we want our base to look like")
    open fun onFailure(response: Response<S>): Result<Nothing> {
        val responseBody = response.errorBody()?.string() ?: ""
        val errorMessage = NetworkErrorParser.extractErrorMessage(responseBody) ?: responseBody
        val exception = when (response.code()) {
            301, 302, 308 -> RedirectException(errorMessage, false)
            401 -> UnauthorizedException(errorMessage, false)
            400, 402 -> ClientException(errorMessage, false)
            403 -> ForbiddenException(errorMessage, false)
            404 -> NotFoundException(errorMessage, false)
            500, 501, 502, 503 -> ServiceUnreachableException(errorMessage, true)
            else -> NetworkException("Unknown Status code", true)
        }
        exception.errorCode = response.code().toString()
        exception.correlationVector = response.headers()[CORRELATION_VECTOR_HEADER]
        exception.requestId = response.headers()[REQUEST_ID_HEADER]
        exception.errorBody = responseBody
        exception.innerErrorCodes = NetworkErrorParser.extractInnerErrorsCodes(exception.errorBody)
        SdkLog.i("HttpError: ${exception.errorCode} body: ${exception.errorBody} cv: ${exception.correlationVector}", exception)
        return Result.Failure(exception)
    }

    fun <S> onRetry(): Result<S> {
        throw LocalNetworkException("Retry Not Supported.")
    }
}