/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.util.Constants.DEFAULT_EXPIRATION_IN_MINUTES
import java.util.Date
import kotlin.math.floor

fun createIatAndExp(expiresIn: Int = DEFAULT_EXPIRATION_IN_MINUTES): Pair<Long, Long> {
    val currentTime = Date().time
    val expiresInMilliseconds = 1000 * 60 * expiresIn
    val expiration = currentTime + expiresInMilliseconds.toLong()
    val exp = floor(expiration / 1000f).toLong()
    val iat = floor(currentTime / 1000f).toLong()
    return Pair(iat, exp)
}