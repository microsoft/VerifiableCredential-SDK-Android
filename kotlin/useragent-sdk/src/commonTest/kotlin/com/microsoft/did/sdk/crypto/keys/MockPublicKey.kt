package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.utilities.MinimalJson

class MockPublicKey(key: JsonWebKey): PublicKey(key) {
    override fun minimumAlphabeticJwk(): String {
        return MinimalJson.serializer.stringify(JsonWebKey.serializer(), this.toJWK())
    }

    override fun toJWK(): JsonWebKey {
        return JsonWebKey(
            kty = "RSA",
            alg = "MOCK",
            kid = kid
        )
    }
}