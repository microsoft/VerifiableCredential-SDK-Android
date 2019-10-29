package com.microsoft.did.sdk.crypto.plugins

import com.microsoft.did.sdk.crypto.models.webCryptoApi.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Provider
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.utilities.ILogger
import java.security.MessageDigest

class ShaSubtleCrypto(logger: ILogger): Subtle(setOf(ShaProvider(logger)), logger), SubtleCrypto {
    class ShaProvider(logger: ILogger) : Provider(logger) {
        override val name: String = "SHA-256"
        override val privateKeyUsage: Set<KeyUsage>? = null
        override val publicKeyUsage: Set<KeyUsage>? = null
        override val symmetricKeyUsage: Set<KeyUsage> = emptySet()

        override fun onDigest(algorithm: Algorithm, data: ByteArray): ByteArray {
            val digest = MessageDigest.getInstance(algorithm.name)
            return digest.digest(data)
        }
    }

}