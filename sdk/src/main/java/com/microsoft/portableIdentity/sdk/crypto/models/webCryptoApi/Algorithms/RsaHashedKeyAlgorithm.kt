package com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi

open class RsaHashedKeyAlgorithm (modulusLength: ULong, publicExponent: ULong, val hash: Algorithm,
                             additionalParams: Map<String, String> = emptyMap()):
                             RsaKeyAlgorithm(modulusLength, publicExponent, additionalParams)