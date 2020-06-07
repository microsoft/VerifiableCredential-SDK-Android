package com.microsoft.did.sdk.crypto.plugins

import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.utilities.serializer.Serializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EllipticCurveSubtleCrypto @Inject constructor(default: SubtleCrypto, serializer: Serializer) :
    Subtle(setOf(Secp256k1Provider(default)), serializer)