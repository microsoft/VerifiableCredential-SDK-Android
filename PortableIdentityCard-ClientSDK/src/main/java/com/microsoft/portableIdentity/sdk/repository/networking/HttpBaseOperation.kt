/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository.networking

import com.microsoft.portableIdentity.sdk.utilities.controlflow.NetworkException
import kotlinx.io.errors.IOException
import okhttp3.ResponseBody
import retrofit2.Response

open class HttpBaseOperation {

    protected suspend fun <S, E> fire(call: suspend () -> Response<S>, parseError: (error: ResponseBody?) -> E): HttpResult<S, E, Exception> {
        try {
            val response = call.invoke()
            if (response.isSuccessful) {
                val body = response.body() ?: return HttpResult.Failure(NetworkException("Fetched data is null."))
                return HttpResult.Success(response.code(), body)
            }
            val error = parseError.invoke(response.errorBody())
            return HttpResult.Error(response.code(), error)
        } catch (exception: IOException) {
            return HttpResult.Failure(NetworkException("unable to perform http operation", exception))
        }
    }

    // generic error body parser that converts error body into a string for now.
    val parseGenericErrorBody = { body: ResponseBody? -> body?.string() ?: "" }
}