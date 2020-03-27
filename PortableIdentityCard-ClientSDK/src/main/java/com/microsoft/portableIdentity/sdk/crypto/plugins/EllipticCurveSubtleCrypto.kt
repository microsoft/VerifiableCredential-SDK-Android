package com.microsoft.portableIdentity.sdk.crypto.plugins

import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.portableIdentity.sdk.crypto.plugins.subtleCrypto.Subtle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EllipticCurveSubtleCrypto @Inject constructor(default: SubtleCrypto): Subtle(setOf(Secp256k1Provider(default)))