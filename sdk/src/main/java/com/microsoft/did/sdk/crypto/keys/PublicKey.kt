package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage

abstract class PublicKey(
        kid: String,
        kty: KeyType,
        use: KeyUse,
        key_ops: List<KeyUsage> = emptyList(),
        alg: String
) : Key(kid, kty, use, key_ops, alg)