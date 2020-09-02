/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import java.util.Date
import kotlin.math.floor

fun createIssuedAndExpiryTime(expiryInSeconds: Int): Pair<Long, Long> {
    val currentTime = Date().time
    val issuedTime = floor(currentTime / 1000f).toLong()
    val expiryInMilliseconds = 1000 * expiryInSeconds
    val expiration = currentTime + expiryInMilliseconds.toLong()
    val expiryTime = floor(expiration / 1000f).toLong()
    return Pair(issuedTime, expiryTime)
}