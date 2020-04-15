/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository.networking

import com.microsoft.portableIdentity.sdk.utilities.SdkLog
import com.microsoft.portableIdentity.sdk.utilities.controlflow.AuthenticationException
import kotlinx.io.errors.IOException
import retrofit2.Response

open class HttpBaseOperation {

    suspend fun <T : Any> fire(call: suspend () -> Response<T>, errorMessage: String): T? {
        val result: HttpResult<T> = getHttpResult(call, errorMessage)
        var data: T? = null

        when (result) {
            is HttpResult.Success ->
                data = result.data
            is HttpResult.Error -> {
                SdkLog.e("Could not do http operation.", result.exception)
            }
        }
        return data
    }

    suspend fun <T : Any> call(call: suspend () -> Response<T>, errorMessage: String): Response<T>? {
        return call.invoke()
    }

    /**
     * TODO(is this a good place for error handling different status codes?)
     */
    private suspend fun <T : Any> getHttpResult(call: suspend () -> Response<T>, errorMessage: String): HttpResult<T> {
        val response = call.invoke()
        if (response.code() == 401) {
            throw AuthenticationException("Http request forbidden.")
        }
        if (response.isSuccessful) return HttpResult.Success(response.body()!!)

        return HttpResult.Error(IOException("Error Occurred during getting safe Api result, Custom ERROR - $errorMessage"))
    }
}