package com.microsoft.useragentSdk.crypto.models.webCryptoApi

class EcKeyGenParams(name: String, val namedCurve: String, additionalParams: Map<String, Any> = emptyMap()): Algorithm(name, additionalParams) {
}