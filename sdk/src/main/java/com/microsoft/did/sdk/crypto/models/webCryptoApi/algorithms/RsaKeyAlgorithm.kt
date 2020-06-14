package com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms

import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants

open class RsaKeyAlgorithm(
    val modulusLength: ULong, val publicExponent: ULong,
    additionalParams: Map<String, String> = emptyMap()
) : Algorithm(W3cCryptoApiConstants.RsaSsaPkcs1V15.value, additionalParams)