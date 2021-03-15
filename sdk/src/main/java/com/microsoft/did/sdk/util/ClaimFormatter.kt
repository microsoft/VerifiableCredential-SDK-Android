/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.util

import java.text.DateFormat

object ClaimFormatter {

    enum class ClaimType {
        DATE,
        TEXT
    }

    fun formatClaimValue(type: String, claimValue: String): String {
        return when (type.asEnumOrDefault(ClaimType.TEXT)) {
            ClaimType.DATE -> formatDateAndTime(claimValue.toLongOrNull())
            ClaimType.TEXT -> claimValue
        }
    }

    fun formatDateAndTime(timestamp: Long?): String {
        if (timestamp == null) return "?"
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(timestamp)
    }

    fun formatDate(timestamp: Long): String {
        return DateFormat.getDateInstance(DateFormat.LONG).format(timestamp * 1000L)
    }
}

inline fun <reified T : Enum<T>> String.asEnumOrDefault(defaultValue: T): T =
    enumValues<T>().firstOrNull { it.name.equals(this, ignoreCase = true) } ?: defaultValue