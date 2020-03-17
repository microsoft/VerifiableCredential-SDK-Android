package com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi

class RsaOaepParams(val label: ByteArray? = null, additionalParams: Map<String, Any> = emptyMap()): Algorithm(W3cCryptoApiConstants.RsaOaep.value, additionalParams) {
}