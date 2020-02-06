package com.microsoft.did.sdk.crypto.models.webCryptoApi

class EcKeyGenParams(val namedCurve: String, additionalParams: Map<String, Any> = emptyMap()): Algorithm(W3cCryptoApiConstants.EcDsa.value, additionalParams) {
}