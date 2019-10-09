package com.microsoft.did.sdk.crypto.models.webCryptoApi.Algorithms

import com.microsoft.did.sdk.crypto.models.webCryptoApi.Algorithm

class AesKeyGenParams(name: String, val length: UShort, additionalParams: Map<String, String> = emptyMap()): Algorithm(name,  additionalParams) {
}