package com.microsoft.did.sdk.utilities

import java.util.*

/**
 * Returns the current time in milliseconds since UNIX epoch
 */
actual fun getCurrentTime(): Long {
    return Date().time
}