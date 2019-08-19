package com.microsoft.useragentSdk.crypto.models.webCryptoApi

interface SubtleCrypto {
    fun encrypt(algorithm: Algorithm,
                key: CryptoKey,
                data: ByteArray): ByteArray

    fun decrypt(algorithm: Algorithm,
                key: CryptoKey,
                data: ByteArray): ByteArray

    fun sign(algorithm: Algorithm,
             key: CryptoKey,
             data: ByteArray): ByteArray

    fun verify(algorithm: Algorithm,
               key: CryptoKey,
               signature: ByteArray,
               data: ByteArray): ByteArray

    fun digest(algorithm: Algorithm,
    data: ByteArray): ByteArray

    fun generateKey(algorithm: Algorithm,
    extractable: Boolean,
    keyUsages: List<KeyUsage>): ByteArray;

    fun deriveKey(algorithm: Algorithm,
                  baseKey: CryptoKey,
                  derivedKeyType: Algorithm,
                  extractable: Boolean,
                  keyUsages: List<KeyUsage> ): ByteArray

    fun deriveBits(algorithm: Algorithm,
                   baseKey: CryptoKey,
                   length: ULong ): ByteArray;

    fun importKey(format: KeyFormat,
                  keyData: KeyData,
                  algorithm: Algorithm,
                  extractable: Boolean,
                  keyUsages: List<KeyUsage>): CryptoKey;

    fun exportKey(format: KeyFormat, key: CryptoKey): ByteArray

    fun wrapKey(format: KeyFormat,
                key: CryptoKey,
                wrappingKey: CryptoKey,
                wrapAlgorithm: Algorithm): ByteArray

    fun unwrapKey(format: KeyFormat,
                  wrappedKey: ByteArray,
                  unwrappingKey: CryptoKey,
                  unwrapAlgorithm: Algorithm,
                  unwrappedKeyAlgorithm: Algorithm,
                  extractable: Boolean,
                  keyUsages: List<KeyUsage> ): CryptoKey;
}