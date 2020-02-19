// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.utilities.ConsoleLogger
import com.microsoft.did.sdk.utilities.IPolymorphicSerialization
import com.microsoft.did.sdk.utilities.MinimalJson
import com.microsoft.did.sdk.utilities.PolymorphicSerialization

class MockPublicKey(key: JsonWebKey): PublicKey(key, ConsoleLogger()) {
    override fun minimumAlphabeticJwk(): String {
        val polymorphicSerialization: IPolymorphicSerialization = PolymorphicSerialization
        return polymorphicSerialization.stringify(JsonWebKey.serializer(), this.toJWK())
//        return MinimalJson.serializer.stringify(JsonWebKey.serializer(), this.toJWK())
    }

    override fun toJWK(): JsonWebKey {
        return JsonWebKey(
            kty = "RSA",
            alg = this.alg,
            kid = kid
        )
    }
}