package com.microsoft.did.sdk.crypto.plugins

import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle

class EllipticCurveSubtleCrypto(default: SubtleCrypto): Subtle(setOf(Secp256k1Provider(default))), SubtleCrypto {
}