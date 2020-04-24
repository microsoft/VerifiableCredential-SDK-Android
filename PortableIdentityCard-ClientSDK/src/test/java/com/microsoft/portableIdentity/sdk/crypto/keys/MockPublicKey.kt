// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.crypto.keys

import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.utilities.Serializer

class MockPublicKey(key: JsonWebKey): PublicKey(key) {
    override fun minimumAlphabeticJwk(): String {
        return Serializer().stringify(JsonWebKey.serializer(), this.toJWK())
    }

    override fun toJWK(): JsonWebKey {
        return JsonWebKey(
            kty = "RSA",
            alg = this.alg,
            kid = kid
        )
    }
}