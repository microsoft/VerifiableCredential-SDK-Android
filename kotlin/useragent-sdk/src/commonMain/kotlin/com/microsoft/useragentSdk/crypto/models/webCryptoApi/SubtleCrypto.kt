package com.microsoft.useragentSdk.crypto.models.webCryptoApi

interface SubtleCrypto {
    fun encrypt(algorithm: AlgorithmIdentifier,
                key: CryptoKey,
                data: ByteArray): ByteArray

    fun decrypt(algorithm: AlgorithmIdentifier,
                key: CryptoKey,
                data: ByteArray): ByteArray

    fun sign(algorithm: AlgorithmIdentifier,
             key: CryptoKey,
             data: ByteArray): ByteArray

    fun verify(algorithm: AlgorithmIdentifier,
               key: CryptoKey,
               signature: ByteArray,
               data: ByteArray): ByteArray

    fun digest(algorithm: AlgorithmIdentifier,
    data: ByteArray): ByteArray

    fun generateKey(algorithm: AlgorithmIdentifier,
    extractable: Boolean,
    keyUsages: List<KeyUsage>): ByteArray;

    fun deriveKey(algorithm: AlgorithmIdentifier,
                  baseKey: CryptoKey,
                  derivedKeyType: AlgorithmIdentifier,
                  extractable: Boolean,
                  keyUsages: List<KeyUsage> ): ByteArray

    fun deriveBits(algorithm: AlgorithmIdentifier,
                   baseKey: CryptoKey,
                   length: ULong ): ByteArray;

    fun importKey(format: KeyFormat,
                  keyData: KeyData,
                  algorithm: AlgorithmIdentifier,
                  extractable: Boolean,
                  keyUsages: List<KeyUsage>): CryptoKey;

    fun exportKey(format: KeyFormat, key: CryptoKey): ByteArray

    fun wrapKey(format: KeyFormat,
                key: CryptoKey,
                wrappingKey: CryptoKey,
                wrapAlgorithm: AlgorithmIdentifier): ByteArray

    fun unwrapKey(format: KeyFormat,
                  wrappedKey: ByteArray,
                  unwrappingKey: CryptoKey,
                  unwrapAlgorithm: AlgorithmIdentifier,
                  unwrappedKeyAlgorithm: AlgorithmIdentifier,
                  extractable: Boolean,
                  keyUsages: List<KeyUsage> ): CryptoKey;
}