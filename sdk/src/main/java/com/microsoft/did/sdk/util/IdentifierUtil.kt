// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import org.erdtman.jcs.JsonCanonicalizer
import java.security.MessageDigest


internal fun multiHash(bytes: ByteArray): ByteArray {
    val digest = MessageDigest.getInstance(W3cCryptoApiConstants.Sha256.value)
    //Prepend the hash value with hash algorithm code and digest length to be in multihash format as expected by Sidetree
    return byteArrayOf(Constants.SIDETREE_MULTIHASH_CODE.toByte(), Constants.SIDETREE_MULTIHASH_LENGTH.toByte()) + digest.digest(bytes)
}

internal fun nonMultiHash(bytes: ByteArray): ByteArray {
    val digest = MessageDigest.getInstance(W3cCryptoApiConstants.Sha256.value)
    return digest.digest(bytes)
}

fun canonicalizeAsByteArray(inputString: String): ByteArray {
    val jsonCanonicalizer = JsonCanonicalizer(inputString)
    return jsonCanonicalizer.encodedUTF8
}