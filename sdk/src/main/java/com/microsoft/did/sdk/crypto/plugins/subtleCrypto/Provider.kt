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

    protected open fun onDeriveBits(algorithm: Algorithm, baseKey: CryptoKey, length: ULong): ByteArray {
        throw UnSupportedOperationException("DeriveBits not supported.")
    }

    protected open fun onExportKey(format: KeyFormat, key: CryptoKey): ByteArray {
        throw UnSupportedOperationException("ExportKey not supported.")
    }

    protected open fun onExportKeyJwk(key: CryptoKey): JsonWebKey {
        throw UnSupportedOperationException("ExportKeyJwk not supported.")
    }

    protected open fun onImportKey(
        format: KeyFormat, keyData: ByteArray, algorithm: Algorithm,
        extractable: Boolean, keyUsages: Set<KeyUsage>
    ): CryptoKey {
        throw UnSupportedOperationException("ImportKey not supported.")
    }

    protected open fun onImportKey(
        format: KeyFormat, keyData: JsonWebKey, algorithm: Algorithm,
        extractable: Boolean, keyUsages: Set<KeyUsage>
    ): CryptoKey {
        throw UnSupportedOperationException("ImportKey not supported.")
    }

    protected open fun checkGenerateKeyParams(algorithm: Algorithm) {
        throw UnSupportedOperationException("GenerateKey params check not implemented")
    }

    open fun checkDerivedKeyParams(algorithm: Algorithm) {
        throw UnSupportedOperationException("DerivedKey params check not implemented")
    }

    fun digest(algorithm: Algorithm, data: ByteArray): ByteArray {
        checkDigest(algorithm)
        return this.onDigest(algorithm, data)
    }

    fun generateKey(algorithm: Algorithm, extractable: Boolean, keyUsages: Set<KeyUsage>): CryptoKey {
        checkGenerateKey(algorithm, keyUsages)
        return onGenerateKey(algorithm, extractable, keyUsages)
    }

    fun generateKeyPair(algorithm: Algorithm, extractable: Boolean, keyUsages: Set<KeyUsage>): CryptoKeyPair {
        checkGenerateKey(algorithm, keyUsages)
        return onGenerateKeyPair(algorithm, extractable, keyUsages)
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

    fun deriveBits(algorithm: Algorithm, baseKey: CryptoKey, length: ULong): ByteArray {
        checkDeriveBits(algorithm, baseKey, length)
        return onDeriveBits(algorithm, baseKey, length)
    }

    fun exportKey(format: KeyFormat, key: CryptoKey): ByteArray {
        checkExportKey(key)
        return onExportKey(format, key)
    }

    fun exportKeyJwk(key: CryptoKey): JsonWebKey {
        checkExportKey(key)
        return onExportKeyJwk(key)
    }

    fun importKey(
        format: KeyFormat, keyData: ByteArray, algorithm: Algorithm, extractable: Boolean,
        keyUsages: Set<KeyUsage>
    ): CryptoKey {
        if (format == KeyFormat.Jwk) {
            throw KeyFormatException("KeyData does not match format")
        }
        checkImportKey(algorithm, keyUsages)
        return onImportKey(format, keyData, algorithm, extractable, keyUsages)
    }

    fun importKey(
        format: KeyFormat, keyData: JsonWebKey, algorithm: Algorithm, extractable: Boolean,
        keyUsages: Set<KeyUsage>
    ): CryptoKey {
        if (format != KeyFormat.Jwk) {
            throw KeyFormatException("KeyData does not match format")
        }
        checkImportKey(algorithm, keyUsages)
        return onImportKey(format, keyData, algorithm, extractable, keyUsages)
    }

    private fun checkDigest(algorithm: Algorithm) {
        checkAlgorithmName(algorithm)
    }

    private fun checkGenerateKey(algorithm: Algorithm, keyUsages: Set<KeyUsage>) {
        checkAlgorithmName(algorithm)
        checkGenerateKeyParams(algorithm)
        if (keyUsages.count() == 0) {
            throw KeyException("Usages cannot be empty when creating a key.")
        }
        val allowedUsages: Set<KeyUsage> = if (this.symmetricKeyUsage != null) {
            this.symmetricKeyUsage!!
        } else {
            this.privateKeyUsage!! union this.publicKeyUsage!!
        }
        this.checkKeyUsages(keyUsages, allowedUsages)
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

    private fun checkDeriveBits(algorithm: Algorithm, baseKey: CryptoKey, length: ULong) {
        checkAlgorithmName(algorithm)
        checkAlgorithmParams(algorithm)
        checkCryptoKey(baseKey, KeyUsage.DeriveBits)
        if (length.rem(8u).compareTo(0u) != 0) {
            throw KeyException("Length is not a multiple of 8")
        }
    }

    private fun checkExportKey(key: CryptoKey) {
        if (!key.extractable) {
            throw KeyException("Key is not extractable")
        }
    }

    private fun checkImportKey(algorithm: Algorithm, keyUsages: Set<KeyUsage>) {
        checkAlgorithmName(algorithm)
        checkAlgorithmParams(algorithm)
        checkImportParams(algorithm)

        if (this.symmetricKeyUsage != null) {
            checkKeyUsages(keyUsages, this.symmetricKeyUsage!!)
        } else {
            // check to see if its a private (or fall back to public)
            try {
                checkKeyUsages(keyUsages, this.privateKeyUsage!!)
            } catch (error: Throwable) {
                checkKeyUsages(keyUsages, this.publicKeyUsage!!)
            }
        }
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