package com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi

class EcdsaParams(val hash: Algorithm, additionalParams: Map<String, Any> = emptyMap()): Algorithm(W3cCryptoApiConstants.EcDsa.value, additionalParams) {
}