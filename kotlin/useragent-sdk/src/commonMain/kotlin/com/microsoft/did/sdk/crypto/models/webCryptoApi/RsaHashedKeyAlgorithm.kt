package com.microsoft.did.sdk.crypto.models.webCryptoApi

class RsaHashedKeyAlgorithm (name: String, modulusLength: ULong, publicExponent: ULong, val hash: Algorithm,
                             additionalParams: Map<String, String> = emptyMap()):
                             RsaKeyAlgorithm(name, modulusLength, publicExponent, additionalParams)