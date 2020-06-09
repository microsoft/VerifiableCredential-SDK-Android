package com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms

open class RsaHashedKeyAlgorithm(
    modulusLength: ULong, publicExponent: ULong, val hash: Algorithm,
    additionalParams: Map<String, String> = emptyMap()
) : RsaKeyAlgorithm(modulusLength, publicExponent, additionalParams)