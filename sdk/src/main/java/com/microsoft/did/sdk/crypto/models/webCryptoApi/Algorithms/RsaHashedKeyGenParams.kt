package com.microsoft.did.sdk.crypto.models.webCryptoApi.Algorithms

import com.microsoft.did.sdk.crypto.models.webCryptoApi.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.RsaKeyAlgorithm

open class RsaHashedKeyGenParams(modulusLength: ULong, publicExponent: ULong,
                            val hash: Algorithm, additionalParams: Map<String, String> = emptyMap()):
    RsaKeyAlgorithm(modulusLength, publicExponent, additionalParams)