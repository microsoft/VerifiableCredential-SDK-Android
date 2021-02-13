// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.identifier

import android.util.Base64
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.DigestAlgorithm
import com.microsoft.did.sdk.util.Constants
import com.nimbusds.jose.jwk.JWK
import org.erdtman.jcs.JsonCanonicalizer
import javax.inject.Inject

class SideTreeHelper @Inject constructor() {

    fun canonicalizeMultiHashEncode(json: String): String {
        val jsonCanonicalizer = JsonCanonicalizer(json)
        val hashed = CryptoOperations.digest(jsonCanonicalizer.encodedUTF8, DigestAlgorithm.Sha256)
        val hashedWithMultiHashInfo = prependMultiHashInfo(hashed)
        return Base64.encodeToString(hashedWithMultiHashInfo, Constants.BASE64_URL_SAFE)
    }

    /**
     * Creates a commitment value that is used by Sidetree to verify the origin of the next request
     */
    fun createCommitmentValue(key: JWK): String {
        return canonicalizeAndDoubleMultiHash(key.toJSONString())
    }

    /**
     * Canonicalize a JSON String and performs a multi hash on it
     */
    private fun canonicalizeAndDoubleMultiHash(json: String): String {
        val jsonCanonicalizer = JsonCanonicalizer(json)
        val hashed = CryptoOperations.digest(jsonCanonicalizer.encodedUTF8, DigestAlgorithm.Sha256)
        val doubleHashed = CryptoOperations.digest(hashed, DigestAlgorithm.Sha256)
        val doubleHashedWithMultiHashInfo = prependMultiHashInfo(doubleHashed)
        return Base64.encodeToString(doubleHashedWithMultiHashInfo, Constants.BASE64_URL_SAFE)
    }

    /**
     * Prepend the hash value with the hash algorithm code and digest length to be in multihash format as expected by Sidetree
     */
    private fun prependMultiHashInfo(bytes: ByteArray): ByteArray {
        return byteArrayOf(Constants.SIDETREE_MULTIHASH_CODE.toByte(), Constants.SIDETREE_MULTIHASH_LENGTH.toByte()) + bytes
    }
}