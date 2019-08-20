package com.microsoft.useragentSdk.crypto.plugins.subtleCrypto

import com.microsoft.useragentSdk.crypto.models.webCryptoApi.*

abstract class Provider {
    public abstract val name: String
    public abstract val privateKeyUsage: Set<KeyUsage>?
    public abstract val publicKeyUsage: Set<KeyUsage>?
    public abstract val symmetricKeyUsage: Set<KeyUsage>?

    protected fun onDigest(algorithm: Algorithm, data: ByteArray): ByteArray {
        throw Error("Digest not supported.")
    }
    protected fun onGenerateKey(algorithm: Algorithm, extractable: Boolean, keyUsages: Set<KeyUsage>): CryptoKey {
        throw Error("GenerateKey not supported.")
    }
    protected fun onSign(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        throw Error("Sign not supported.")
    }
    protected fun onVerify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        throw Error("Verify not supported.")
    }
    protected fun onEncrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        throw Error("Encrypt not supported.")
    }
    protected fun onDecrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        throw Error("Decrypt not supported.")
    }
    protected fun onDeriveBits(algorithm: Algorithm, baseKey: CryptoKey, length: ULong): ByteArray {
        throw Error("DeriveBits not supported.")
    }
    protected fun onExportKey(format: KeyFormat, key: CryptoKey): KeyData {
        throw Error("ExportKey not supported.")
    }
    protected fun onImportKey(format: KeyFormat, keyData: KeyData, algorithm: Algorithm,
                              extractable: Boolean, keyUsages: Set<KeyUsage>): CryptoKey {
        throw Error("ImportKey not supported.")
    }

    protected fun checkGenerateKeyParams(algorithm: Algorithm) {
        throw Error("GenerateKey params check not implemented")
    }

    public fun digest(algorithm: Algorithm, data: ByteArray): ByteArray {
        checkDigest(algorithm)
        return this.onDigest(algorithm, data)
    }
    public fun generateKey(algorithm: Algorithm, extractable: Boolean, keyUsages: Set<KeyUsage>): CryptoKey {
        checkGenerateKey(algorithm, extractable, keyUsages)
        return onGenerateKey(algorithm, extractable, keyUsages)
    }
    public fun sign(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        checkSign(algorithm, key)
        return onSign(algorithm, key, data)
    }
    public fun verify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        checkVerify(algorithm, key)
        return onVerify(algorithm, key, signature, data)
    }
    public fun encrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        checkEncrypt(algorithm, key)
        return onEncrypt(algorithm, key, data)
    }
    public fun decrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        checkDecrypt(algorithm, key)
        return onDecrypt(algorithm, key, data)
    }
    public fun deriveBits(algorithm: Algorithm, baseKey: CryptoKey, length: ULong): ByteArray {
        checkDeriveBits(algorithm, baseKey, length)
        return  onDeriveBits(algorithm, baseKey, length)
    }
    public fun exportKey(format: KeyFormat, key: CryptoKey): KeyData {
        checkExportKey(format, key)
        return onExportKey(format, key)
    }
    public fun importKey(format: KeyFormat, keyData: KeyData, algorithm: Algorithm, extractable: Boolean,
                         keyUsages: Set<KeyUsage>): CryptoKey {
        checkImportKey(format, keyData, algorithm, extractable, keyUsages)
        return onImportKey(format, keyData, algorithm, extractable, keyUsages)
    }


    private fun checkDigest(algorithm: Algorithm) {
        checkAlgorithmName(algorithm)
    }
    private fun checkGenerateKey(algorithm: Algorithm, extractable: Boolean, keyUsages: Set<KeyUsage>) {
        checkAlgorithmName(algorithm)
        checkGenerateKeyParams(algorithm)
        if (keyUsages.count() == 0) {
            throw Error("Usages cannot be empty when creating a key.")
        }
        var allowedUsages: Set<KeyUsage> = if (this.symmetricKeyUsage != null) {
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
            throw Error("Length is not a multiple of 8")
        }
    }
    private fun checkExportKey(format: KeyFormat, key: CryptoKey) {
        if (!key.extractable) {
            throw Error("Key is not extractable")
        }
    }
    private fun checkImportKey(format: KeyFormat, keyData: KeyData, algorithm: Algorithm, extractable: Boolean, keyUsages: Set<KeyUsage>) {
        checkKeyData(format, keyData)
        checkAlgorithmName(algorithm)
        checkAlgorithmParams(algorithm)
        checkImportParams(algorithm)

        if (this.symmetricKeyUsage != null) {
            checkKeyUsages(keyUsages, this.symmetricKeyUsage!!)
        } else {
            // check to see if its a private (or fall back to public)
            try {
                checkKeyUsages(keyUsages, this.privateKeyUsage!!)
            } catch (error: Error) {
                checkKeyUsages(keyUsages, this.publicKeyUsage!!)
            }
        }
    }

    protected open fun checkAlgorithmName(algorithm: Algorithm) {
        if (algorithm.name.toLowerCase() != this.name.toLowerCase()) {
            throw Error("Unrecognized Algorithm ${algorithm.name}")
        }
    }

    protected open fun checkAlgorithmParams(algorithm: Algorithm) {
        // there are no generic checks to perform
    }

    protected fun checkKeyUsages(usages: Set<KeyUsage>, allowed: Set<KeyUsage>) {
        val forbiddenUsages = usages - allowed
        if (forbiddenUsages.isNotEmpty()) {
            throw Error("Key Usages contains forbidden Key Usage: ${forbiddenUsages.joinToString()}")
        }
    }

    protected open fun checkCryptoKey(key: CryptoKey, keyUsage: KeyUsage) {
        checkAlgorithmName(key.algorithm)
        if (!key.usages.contains(keyUsage)) {
            throw Error("Key does not allow ${keyUsage.name}")
        }
    }

    protected open fun checkImportParams(algorithm: Algorithm) {
        // there are no generic checks to perform
    }

    protected open fun checkKeyData(format: KeyFormat, keyData: KeyData) {
        if (keyData.data == null && keyData.jwk == null) {
            throw Error("keyData cannot be empty")
        }
        when (format) {
            KeyFormat.Jwk -> {
                if (keyData.jwk == null) {
                    throw Error("keyData is not a Json Web Token")
                }
            }
            else -> {
                if (keyData.data == null) {
                    throw Error("keyData is not a ByteArray")
                }
            }
        }
    }
}