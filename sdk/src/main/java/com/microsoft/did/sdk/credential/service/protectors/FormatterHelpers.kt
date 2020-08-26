/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.util.controlflow.FormatterException
import java.util.Date
import kotlin.math.floor

fun createIssuedAndExpiryTime(expiryInSeconds: Int): Pair<Long, Long> {
    val currentTime = Date().time
    val issuedTime = floor(currentTime / 1000f).toLong()
    if (expiryInSeconds == -1) {
        throw FormatterException("Expiry for OIDC Responses cannot be negative")
    }
    val expiryInMilliseconds = 1000 * expiryInSeconds
    val expiration = currentTime + expiryInMilliseconds.toLong()
    val expiryTime = floor(expiration / 1000f).toLong()
    return Pair(issuedTime, expiryTime)
}