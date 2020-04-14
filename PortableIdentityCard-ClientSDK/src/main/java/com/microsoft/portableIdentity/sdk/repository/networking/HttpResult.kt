/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository.networking

sealed class HttpResult<out S, out E, out F> {
    class Success<out S>(val code: Int, val body: S): HttpResult<S, Nothing, Nothing>()
    class Error<out E>(val code: Int, val body: E): HttpResult<Nothing, E, Nothing>()
    class Failure<out F>(val body: F): HttpResult<Nothing, Nothing, F>()
}