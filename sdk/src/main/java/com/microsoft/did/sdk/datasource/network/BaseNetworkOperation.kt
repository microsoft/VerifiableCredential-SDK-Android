/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network

import com.microsoft.did.sdk.util.controlflow.LocalNetworkException
import com.microsoft.did.sdk.util.controlflow.NetworkException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.ServiceErrorException
import com.microsoft.did.sdk.util.controlflow.ServiceUnreachableException
import com.microsoft.did.sdk.util.controlflow.UnauthorizedException
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
            val response = call.invoke()
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
        val requestId = response.headers()["request-id"] ?: "?"
        val correlationVector = response.headers()["ms-cv"] ?: "?"
        return when (response.code()) {
            401 -> Result.Failure(
                UnauthorizedException(
                    requestId,
                    defaultErrorMessage(response.code(), requestId, correlationVector, response.errorBody()?.string() ?: ""),
                    false
                )
            )
            402, 403, 404 -> Result.Failure(
                ServiceErrorException(
                    requestId,
                    defaultErrorMessage(response.code(), requestId, correlationVector, response.errorBody()?.string() ?: ""),
                    false
                )
            )
            500, 501, 502, 503 -> Result.Failure(
                ServiceUnreachableException(
                    requestId,
                    defaultErrorMessage(response.code(), requestId, correlationVector, response.errorBody()?.string() ?: ""),
                    true
                )
            )
            else -> Result.Failure(NetworkException(requestId, "Unknown Status code ${response.code()}", true))
        }
    }

    private fun defaultErrorMessage(httpCode: Int, requestId: String, correlationVector: String, errorBody: String): String {
        return "RequestId: $requestId\nCorrelationVector: $correlationVector\nHttp code: $httpCode\nErrorBody: $errorBody"
    }

    fun <S> onRetry(): Result<S> {
        throw LocalNetworkException("Retry Not Supported.")
    }
}