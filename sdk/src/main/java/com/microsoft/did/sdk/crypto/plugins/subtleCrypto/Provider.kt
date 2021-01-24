package com.microsoft.did.sdk.crypto.plugins.subtleCrypto

import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKeyPair
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.util.controlflow.KeyException
import com.microsoft.did.sdk.util.controlflow.KeyFormatException
import com.microsoft.did.sdk.util.controlflow.UnSupportedOperationException
import com.microsoft.did.sdk.util.controlflow.UnSupportedAlgorithmException
import java.util.Locale

abstract class Provider {
    abstract val name: String
    abstract val privateKeyUsage: Set<KeyUsage>?
    abstract val publicKeyUsage: Set<KeyUsage>?
    abstract val symmetricKeyUsage: Set<KeyUsage>?

    protected open fun onDigest(algorithm: Algorithm, data: ByteArray): ByteArray {
        throw UnSupportedOperationException("Digest not supported.")
    }

    protected open fun onGenerateKey(algorithm: Algorithm, extractable: Boolean, keyUsages: Set<KeyUsage>): CryptoKey {
        throw UnSupportedOperationException("GenerateKey not supported.")
    }

    protected open fun onGenerateKeyPair(algorithm: Algorithm, extractable: Boolean, keyUsages: Set<KeyUsage>): CryptoKeyPair {
        throw UnSupportedOperationException("GenerateKeyPair not supported.")
    }

    protected open fun onSign(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        throw UnSupportedOperationException("Sign not supported.")
    }

    protected open fun onVerify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        throw UnSupportedOperationException("Verify not supported.")
    }

    protected open fun onEncrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        throw UnSupportedOperationException("Encrypt not supported.")
    }

    protected open fun onDecrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        throw UnSupportedOperationException("Decrypt not supported.")
    }

    fun digest(algorithm: Algorithm, data: ByteArray): ByteArray {
        checkDigest(algorithm)
        return this.onDigest(algorithm, data)
    }

    fun sign(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        checkSign(algorithm, key)
        return onSign(algorithm, key, data)
    }

    fun verify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        checkVerify(algorithm, key)
        return onVerify(algorithm, key, signature, data)
    }

    fun encrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        checkEncrypt(algorithm, key)
        return onEncrypt(algorithm, key, data)
    }

    fun decrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        checkDecrypt(algorithm, key)
        return onDecrypt(algorithm, key, data)
    }

    private fun checkDigest(algorithm: Algorithm) {
        checkAlgorithmName(algorithm)
    }

    private fun checkSign(algorithm: Algorithm, key: CryptoKey) {
        checkAlgorithmName(algorithm)
        checkAlgorithmParams(algorithm)
        checkCryptoKey(key, KeyUsage.Sign)
    }

    private fun checkVerify(algorithm: Algorithm, key: CryptoKey) {
        checkAlgorithmName(algorithm)
        checkAlgorithmParams(algorithm)
        checkCryptoKey(key, KeyUsage.Verify)
    }

    private fun checkEncrypt(algorithm: Algorithm, key: CryptoKey) {
        checkAlgorithmName(algorithm)
        checkAlgorithmParams(algorithm)
        checkCryptoKey(key, KeyUsage.Encrypt)
    }

    private fun checkDecrypt(algorithm: Algorithm, key: CryptoKey) {
        this.checkAlgorithmName(algorithm)
        this.checkAlgorithmParams(algorithm)
        this.checkCryptoKey(key, KeyUsage.Decrypt)
    }

    protected open fun checkAlgorithmName(algorithm: Algorithm) {
        if (algorithm.name.toLowerCase(Locale.ENGLISH) != this.name.toLowerCase(Locale.ENGLISH)) {
            throw UnSupportedAlgorithmException("Unrecognized Algorithm ${algorithm.name}")
        }
    }

    protected open fun checkAlgorithmParams(algorithm: Algorithm) {
        // there are no generic checks to perform
    }

    private fun checkKeyUsages(usages: Set<KeyUsage>, allowed: Set<KeyUsage>) {
        val forbiddenUsages = usages - allowed
        if (forbiddenUsages.isNotEmpty()) {
            throw KeyException("Key Usages contains forbidden Key Usage: ${forbiddenUsages.joinToString { use -> use.value }}")
        }
    }

    open fun checkCryptoKey(key: CryptoKey, keyUsage: KeyUsage) {
        checkAlgorithmName(key.algorithm)
        if (!key.usages.contains(keyUsage)) {
            throw KeyException("Key does not allow ${keyUsage.name}")
        }
    }

    protected open fun checkImportParams(algorithm: Algorithm) {
        // there are no generic checks to perform
    }
}