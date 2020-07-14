package com.microsoft.did.sdk.crypto.models

import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.util.controlflow.AlgorithmException

enum class Sha(val algorithm: Algorithm) {
    SHA1(Algorithm(W3cCryptoApiConstants.Sha1.value)),
    SHA224(Algorithm(W3cCryptoApiConstants.Sha224.value)),
    SHA256(Algorithm(W3cCryptoApiConstants.Sha256.value)),
    SHA384(Algorithm(W3cCryptoApiConstants.Sha384.value)),
    SHA512(Algorithm(W3cCryptoApiConstants.Sha512.value));

    companion object {
        fun get(length: Int): Algorithm {
            return when (length) {
                1 -> SHA1.algorithm
                224 -> SHA224.algorithm
                256 -> SHA256.algorithm
                384 -> SHA384.algorithm
                512 -> SHA512.algorithm
                else -> throw AlgorithmException("No SHA at this length.")
            }
        }
    }
}