/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.util

import java.text.DateFormat
import java.util.Locale

object ClaimFormatter {

    fun formatClaimValue(type: String, claimValue: String): String {
        return when(type.toLowerCase(Locale.ENGLISH)) {
            "date" -> formatDate(claimValue.toLong())
            else -> claimValue
        }
    }

    fun formatDate(timestamp: Long): String {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(timestamp)
    }
}