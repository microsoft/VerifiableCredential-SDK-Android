package com.microsoft.useragentSdk.crypto.plugins.subtleCrypto

import com.microsoft.useragentSdk.crypto.models.webCryptoApi.Algorithm
import com.microsoft.useragentSdk.crypto.models.webCryptoApi.CryptoKey
import com.microsoft.useragentSdk.crypto.models.webCryptoApi.KeyUsage

abstract class Provider {
    public abstract val name: String
    public abstract val privateKeyUsage: Set<KeyUsage>?
    public abstract val publicKeyUsage: Set<KeyUsage>?
    public abstract val keyUsage: Set<KeyUsage>?

    protected abstract fun onDigest(algorithm: Algorithm, data: ByteArray): ByteArray
    // Output should be a PrivateKey or SymmetricKey
    protected abstract fun onGenerateKey(algorithm: Algorithm, extractable: Boolean, keyUsages: List<KeyUsage>): CryptoKey
    protected abstract fun onSign(algorithm: Algorithm, key: CryptoKey)

    protected abstract fun checkGenerateKeyParams(algorithm: Algorithm)

    public fun digest(algorithm: Algorithm, data: ByteArray): ByteArray {
        checkDigest(algorithm, data)
        return this.onDigest(algorithm, data)
    }
    public fun generateKey(algorithm: Algorithm, extractable: Boolean, keyUsages: List<KeyUsage>): CryptoKey {
        checkGenerateKey(algorithm, extractable, keyUsages)
        return onGenerateKey(algorithm, extractable, keyUsages)
    }

    private fun checkDigest(algorithm: Algorithm, data: ByteArray) {
        checkAlgorithmName(algorithm)
    }
    private fun checkGenerateKey(algorithm: Algorithm, extractable: Boolean, keyUsages: List<KeyUsage>) {
        checkAlgorithmName(algorithm)
        checkGenerateKeyParams(algorithm)
        if (keyUsages.count() == 0) {
            throw Error("Usages cannot be empty when creating a key.")
        }
        var allowedUsages: Set<KeyUsage> = emptySet()
        if (this.keyUsage != null) {
            allowedUsages + this.keyUsage
        } else {
            allowedUsages + this.privateKeyUsage + this.publicKeyUsage
        }
        this.checkKeyUsages(allowedUsages)
    }
}