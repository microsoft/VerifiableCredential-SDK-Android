// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import org.erdtman.jcs.JsonCanonicalizer

object IdentifierUtil {
    /**
     * Prepend the hash value with hash algorithm code and digest length to be in multihash format as expected by Sidetree
     */
    fun prependMultiHashInfo(bytes: ByteArray): ByteArray {
        return byteArrayOf(Constants.SIDETREE_MULTIHASH_CODE.toByte(), Constants.SIDETREE_MULTIHASH_LENGTH.toByte()) + bytes
    }

    fun canonicalizeToByteArray(inputString: String): ByteArray {
        val jsonCanonicalizer = JsonCanonicalizer(inputString)
        return jsonCanonicalizer.encodedUTF8
    }
}