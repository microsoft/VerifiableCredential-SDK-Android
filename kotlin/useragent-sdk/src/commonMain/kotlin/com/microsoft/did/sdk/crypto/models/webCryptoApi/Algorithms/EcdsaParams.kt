package com.microsoft.did.sdk.crypto.models.webCryptoApi

class EcdsaParams(name: String, val hash: Algorithm, additionalParams: Map<String, Any> = emptyMap()): Algorithm(name, additionalParams) {
}