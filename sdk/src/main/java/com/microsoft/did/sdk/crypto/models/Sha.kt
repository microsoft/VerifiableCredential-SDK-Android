package com.microsoft.did.sdk.crypto.models

import com.microsoft.did.sdk.crypto.models.webCryptoApi.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.utilities.controlflow.AlgorithmException


// convenience class for SHA algorithms
class Sha(algorithm: Algorithm) {
    companion object {
        val Sha1 = Algorithm(
            name = W3cCryptoApiConstants.Sha1.value
        )
        val Sha224 = Algorithm(
            name = W3cCryptoApiConstants.Sha224.value
        )
        val Sha256 = Algorithm(
            name = W3cCryptoApiConstants.Sha256.value
        )
        val Sha384 = Algorithm(
            name = W3cCryptoApiConstants.Sha384.value
        )
        val Sha512 = Algorithm(
            name = W3cCryptoApiConstants.Sha512.value
        )
        fun get(length: Int): Algorithm {
            return when (length) {
                1 -> Sha1
                224 -> Sha224
                256 -> Sha256
                384 -> Sha384
                512 -> Sha512
                else -> throw AlgorithmException("No SHA at this length.")
            }
        }
    }
}