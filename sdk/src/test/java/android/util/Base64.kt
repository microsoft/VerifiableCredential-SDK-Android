// Copyright (c) Microsoft Corporation. All rights reserved
@file:JvmName("Base64")
@file:Suppress("unused")

package android.util

import java.util.Base64

/**
 * Android classes are not available in a test environment. This class serves as a mock of the android.util class.
 *
 * Tests will automatically use this Base64 encoder.
 *
 * CAUTION: Not all available methods and flags are implemented.
 */
object Base64 {
    @JvmStatic
    fun encodeToString(input: ByteArray?, flags: Int): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input)
    }

    @JvmStatic
    fun decode(str: String?, flags: Int): ByteArray {
        return Base64.getUrlDecoder().decode(str)
    }
}