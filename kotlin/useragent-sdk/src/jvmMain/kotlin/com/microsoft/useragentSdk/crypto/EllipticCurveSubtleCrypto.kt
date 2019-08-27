package com.microsoft.useragentSdk.crypto

import com.microsoft.useragentSdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.useragentSdk.crypto.plugins.subtleCrypto.Subtle

class EllipticCurveSubtleCrypto: Subtle(setOf(Secp256k1Provider())), SubtleCrypto {
}