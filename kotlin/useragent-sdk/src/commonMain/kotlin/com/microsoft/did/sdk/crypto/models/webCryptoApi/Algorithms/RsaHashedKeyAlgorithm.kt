package com.microsoft.did.sdk.crypto.models.webCryptoApi

val rsaPkcs = W3cCryptoApiConstants.RsaSsaPkcs1V15.value

class RsaHashedKeyAlgorithm (name: String = rsaPkcs, modulusLength: ULong, publicExponent: ULong, val hash: Algorithm,
                             additionalParams: Map<String, String> = emptyMap()):
                             RsaKeyAlgorithm(name, modulusLength, publicExponent, additionalParams)