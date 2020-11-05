// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.util.defaultTestSerializer

class MockPublicKey(key: JsonWebKey) : PublicKey(key) {
    override fun minimumAlphabeticJwk(): String {
        return defaultTestSerializer.encodeToString(JsonWebKey.serializer(), this.toJWK())
    }

    override fun toJWK(): JsonWebKey {
        return JsonWebKey(
            kty = "RSA",
            alg = this.alg,
            kid = kid
        )
    }
}