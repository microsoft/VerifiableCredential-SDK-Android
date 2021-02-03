// Copyright (c) Microsoft Corporation. All rights reserved
@file:JvmName("Base64")

package android.util

import java.util.Base64

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