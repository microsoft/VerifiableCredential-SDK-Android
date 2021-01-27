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
import java.security.Key
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Locale

abstract class Provider {
    protected open fun digest(data: ByteArray): ByteArray {
        throw UnSupportedOperationException("Digest not supported.")
    }

    protected open fun generateKey(): Key {
        throw UnSupportedOperationException("GenerateKey not supported.")
    }

    protected open fun generateKeyPair(): KeyPair {
        throw UnSupportedOperationException("GenerateKeyPair not supported.")
    }

    protected open fun sign(key: PrivateKey, data: ByteArray): ByteArray {
        throw UnSupportedOperationException("Sign not supported.")
    }

    protected open fun verify(key: PublicKey, signature: ByteArray, data: ByteArray): Boolean {
        throw UnSupportedOperationException("Verify not supported.")
    }

    protected open fun encrypt(key: CryptoKey, data: ByteArray): ByteArray {
        throw UnSupportedOperationException("Encrypt not supported.")
    }

    protected open fun decrypt(key: CryptoKey, data: ByteArray): ByteArray {
        throw UnSupportedOperationException("Decrypt not supported.")
    }
}