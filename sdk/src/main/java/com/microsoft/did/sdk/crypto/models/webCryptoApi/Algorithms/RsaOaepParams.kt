package com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms

import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants

class RsaOaepParams(val label: ByteArray? = null, additionalParams: Map<String, Any> = emptyMap()) :
    Algorithm(W3cCryptoApiConstants.RsaOaep.value, additionalParams)