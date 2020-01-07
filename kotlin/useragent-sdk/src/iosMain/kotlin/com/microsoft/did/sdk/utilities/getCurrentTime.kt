package com.microsoft.did.sdk.utilities

import platform.UIKit.UIDevice
import platform.posix.time
/**
 * Returns the current time in milliseconds since UNIX epoch
 */
actual fun getCurrentTime(): Long {
    return time(null) * 1000
}