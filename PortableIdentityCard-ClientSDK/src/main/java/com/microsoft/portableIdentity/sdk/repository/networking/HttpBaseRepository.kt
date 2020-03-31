/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository.networking

import com.microsoft.portableIdentity.sdk.utilities.SdkLog
import kotlinx.io.errors.IOException
import retrofit2.Response

open class HttpBaseRepository{

    suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>, errorMessage: String): T? {

        val result : HttpResult<T> = safeApiResult(call,errorMessage)
        var data : T? = null

        when(result) {
            is HttpResult.Success ->
                data = result.data
            is HttpResult.Error -> {
                SdkLog.e("1.DataRepository", result.exception)
            }
        }


        return data

    }

    private suspend fun <T: Any> safeApiResult(call: suspend ()-> Response<T>, errorMessage: String) : HttpResult<T>{
        val response = call.invoke()
        if(response.isSuccessful) return HttpResult.Success(response.body()!!)

        return HttpResult.Error(IOException("Error Occurred during getting safe Api result, Custom ERROR - $errorMessage"))
    }
}