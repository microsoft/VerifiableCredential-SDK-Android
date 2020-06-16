/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import java.util.*
import kotlin.math.floor

fun createIatAndExp(expiryInSeconds: Int): Pair<Long, Long?> {
    val currentTime = Date().time
    val iat = floor(currentTime / 1000f).toLong()
    if (expiryInSeconds == -1) {
        return Pair(iat, null)
    }
    val expiryInMilliseconds = 1000 * expiryInSeconds
    val expiration = currentTime + expiryInMilliseconds.toLong()
    val exp = floor(expiration / 1000f).toLong()
    return Pair(iat, exp)
}