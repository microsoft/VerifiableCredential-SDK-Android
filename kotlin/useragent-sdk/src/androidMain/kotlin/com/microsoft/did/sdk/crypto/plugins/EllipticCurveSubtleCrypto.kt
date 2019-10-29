package com.microsoft.did.sdk.crypto.plugins

import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.utilities.ILogger

class EllipticCurveSubtleCrypto(default: SubtleCrypto, logger: ILogger): Subtle(setOf(Secp256k1Provider(default, logger)), logger), SubtleCrypto {
}