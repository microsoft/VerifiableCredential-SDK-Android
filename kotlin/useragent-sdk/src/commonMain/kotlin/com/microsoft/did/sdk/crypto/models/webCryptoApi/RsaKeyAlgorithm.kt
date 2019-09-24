package com.microsoft.did.sdk.crypto.models.webCryptoApi

class RsaKeyAlgorithm(name: String, val modulusLength: ULong, val publicExponent: ULong,
                      additionalParams: Map<String, String> = emptyMap()): Algorithm(name, additionalParams)