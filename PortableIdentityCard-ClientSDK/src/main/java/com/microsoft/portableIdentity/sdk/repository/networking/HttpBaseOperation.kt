/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository.networking

import retrofit2.Response

open class HttpBaseOperation {

    suspend fun <T : Any> fire(call: suspend () -> Response<T>, errorMessage: String): T? {
        val response = call.invoke()

        return if (response.isSuccessful) response.body() else null
    }
}