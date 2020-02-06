package com.microsoft.did.sdk.utilities

import java.util.*

/**
 * Returns the current time in milliseconds since UNIX epoch
 */
fun getCurrentTime(): Long {
    return Date().time
}