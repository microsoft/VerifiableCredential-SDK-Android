/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network

import com.microsoft.did.sdk.util.controlflow.*
import kotlinx.io.IOException
import retrofit2.Response

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
            return Result.Failure(NetworkException("Failed to send request.", exception))
        }
    }

    open fun onSuccess(response: Response<S>): Result<T> {
        // TODO("how do we want to handle null bodies")
        // TODO("how to not suppress this warning")
        @Suppress("UNCHECKED_CAST")
        val transformedPayload = (response.body() ?: throw NetworkException("Body of Response is null.")) as T
git         return Result.Success(transformedPayload)
    }

    // TODO("what do we want our base to look like")
    open fun onFailure(response: Response<S>): Result<Nothing> {
        return when (response.code()) {
            401 -> Result.Failure(UnauthorizedException("${response.code()}: ${response.errorBody().toString()}"))
            402, 403, 404 -> Result.Failure(ServiceErrorException("${response.code()}: ${response.errorBody().toString()}"))
            500, 501, 502, 503 -> Result.Failure(ServiceUnreachableException("${response.code()}: ${response.errorBody().toString()}"))
            else -> Result.Failure(NetworkException("Unknown Status code ${response.code()}"))
        }
    }

    fun <S> onRetry(): Result<S> {
        throw NetworkException("Retry Not Supported.")
    }
}