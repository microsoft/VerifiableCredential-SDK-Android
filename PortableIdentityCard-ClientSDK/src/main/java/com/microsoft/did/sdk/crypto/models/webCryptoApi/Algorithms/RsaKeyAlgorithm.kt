package com.microsoft.did.sdk.crypto.models.webCryptoApi

open class RsaKeyAlgorithm(val modulusLength: ULong, val publicExponent: ULong,
                      additionalParams: Map<String, String> = emptyMap()): Algorithm(W3cCryptoApiConstants.RsaSsaPkcs1V15.value, additionalParams)