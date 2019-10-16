package com.microsoft.did.sdk.crypto.plugins

import com.microsoft.did.sdk.crypto.models.webCryptoApi.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Provider
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import java.security.MessageDigest

class ShaSubtleCrypto: Subtle(setOf(ShaProvider())), SubtleCrypto {
    class ShaProvider() : Provider() {
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