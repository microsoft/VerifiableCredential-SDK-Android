package com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms

import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants

class EcdsaParams(val hash: Algorithm, additionalParams: Map<String, Any> = emptyMap()) :
    Algorithm(W3cCryptoApiConstants.EcDsa.value, additionalParams)