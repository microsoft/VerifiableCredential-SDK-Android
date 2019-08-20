package com.microsoft.useragentSdk.crypto.plugins.subtleCrypto

import com.microsoft.useragentSdk.crypto.models.webCryptoApi.*
import com.microsoft.useragentSdk.crypto.models.webCryptoApi.SubtleCrypto

/**
 * sourced from https://github.com/PeculiarVentures/webcrypto-core/blob/master/src/subtle.ts
 */
class SubtleCrypto(providers: Set<Provider> = emptySet()): SubtleCrypto {
    val provider = providers.map({
        Pair<String, Provider>(it.name.toLowerCase(), it)
    }).toMap()

    private fun getProvider(algorithm: String): Provider {
        return provider[algorithm.toLowerCase()] ?: throw Error("Unknown algorithm $algorithm")
    }

    override fun encrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun decrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sign(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun verify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun digest(algorithm: Algorithm, data: ByteArray): ByteArray {
        val provider = getProvider(algorithm.name)
        return provider.digest(algorithm, data)
    }

    override fun generateKey(algorithm: Algorithm, extractable: Boolean, keyUsages: List<KeyUsage>): ByteArray {
        val provider = getProvider(algorithm.name)
        return provider.generateKey()
    }
    override fun generateKeyPair(algorithm: Algorithm, extractable: Boolean, keyUsages: List<KeyUsage>): CryptoKeyPair {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deriveKey(
        algorithm: Algorithm,
        baseKey: CryptoKey,
        derivedKeyType: Algorithm,
        extractable: Boolean,
        keyUsages: List<KeyUsage>
    ): ByteArray {
        val provider = getProvider(algorithm.name)
        return provider.deriveBits(algorithm, baseKey, )
    }

    override fun deriveBits(algorithm: Algorithm, baseKey: CryptoKey, length: ULong): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun importKey(
        format: KeyFormat,
        keyData: ByteArray,
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: List<KeyUsage>
    ): CryptoKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun importKey(
        format: KeyFormat,
        keyData: JsonWebKey,
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: List<KeyUsage>
    ): CryptoKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exportKey(format: KeyFormat, key: CryptoKey): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exportKeyJwk(key: CryptoKey): JsonWebKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun wrapKey(
        format: KeyFormat,
        key: CryptoKey,
        wrappingKey: CryptoKey,
        wrapAlgorithm: Algorithm
    ): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unwrapKey(
        format: KeyFormat,
        wrappedKey: ByteArray,
        unwrappingKey: CryptoKey,
        unwrapAlgorithm: Algorithm,
        unwrappedKeyAlgorithm: Algorithm,
        extractable: Boolean,
        keyUsages: List<KeyUsage>
    ): CryptoKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}