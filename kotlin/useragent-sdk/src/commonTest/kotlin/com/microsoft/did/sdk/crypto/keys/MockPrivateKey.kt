package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey

class MockPrivateKey(key: JsonWebKey): PrivateKey(key) {
    override fun getPublicKey(): PublicKey {
        return MockPublicKey(
            this.toJWK()
        )
    }

    override fun toJWK(): JsonWebKey {
        return JsonWebKey(
            kty = "RSA",
            alg = this.alg,
            kid = kid
        )
    }
}