package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.keys.rsa.RsaPublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey

class AndroidPrivateKey private constructor (jwk: JsonWebKey): PrivateKey(jwk) {

    override fun getPublicKey(): PublicKey {
        return RsaPublicKey(
            JsonWebKey(
                kty = this.kty.value,
                alg = this.alg
        ))
    }
}