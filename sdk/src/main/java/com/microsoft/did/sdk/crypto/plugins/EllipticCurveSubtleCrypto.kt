package com.microsoft.did.sdk.crypto.plugins

import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EllipticCurveSubtleCrypto @Inject constructor(default: SubtleCrypto, serializer: Json) :
    Subtle(setOf(Secp256k1Provider(default)), serializer)