package com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms

class AesKeyGenParams(name: String, val length: UShort, additionalParams: Map<String, String> = emptyMap()) :
    Algorithm(name, additionalParams)