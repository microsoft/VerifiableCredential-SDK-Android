package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey

class AndroidPrivateKey(jwk: JsonWebKey): PrivateKey(jwk) {
}