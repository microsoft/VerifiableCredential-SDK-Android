package com.microsoft.portableIdentity.sdk.crypto.plugins

import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.portableIdentity.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.portableIdentity.sdk.utilities.ILogger

class EllipticCurveSubtleCrypto(default: SubtleCrypto, logger: ILogger): Subtle(setOf(Secp256k1Provider(default, logger)), logger), SubtleCrypto {
}