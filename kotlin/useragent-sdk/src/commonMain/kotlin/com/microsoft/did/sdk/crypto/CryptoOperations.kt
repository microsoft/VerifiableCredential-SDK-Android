package com.microsoft.did.sdk.crypto

import com.microsoft.did.sdk.crypto.keyStore.IKeyStore
import com.microsoft.did.sdk.crypto.keyStore.getDefaultKeyStore
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoFactory
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.getDefaultSubtle

/**
 * Class that encompasses all of Crypto
 * @param subtleCrypto primitives for operations.
 * @param keyStore specific keyStore that securely holds keys.
 */
class CryptoOperations(subtleCrypto: SubtleCrypto = getDefaultSubtle(), val keyStore: IKeyStore = getDefaultKeyStore()) {

    val subtleCryptoFactory = SubtleCryptoFactory(subtleCrypto);

    /**
     * Sign payload with key stored in keyStore.
     * @param payload to sign.
     * @param signingKeyReference reference to key stored in keystore.
     */
    fun sign(payload: ByteArray, signingKeyReference: String, algorithm: Algorithm? = null): ByteArray {
        TODO("Not implemented")
    }

    /**
     * Verify payload with key stored in keyStore.
     */
    fun verify(payload: ByteArray, signature: ByteArray, signingKeyReference: String) {
        TODO("Not implemented")
    }

    /**
     * Encrypt payload with key stored in keyStore.
     */
    fun encrypt() {
        TODO("Not implemented")
    }

    /**
     * Decrypt payload with key stored in keyStore.
     */
    fun decrypt() {
        TODO("Not implemented")
    }

    /**
     * Generate a pairwise key.
     * @param seed to be used to create pairwise key.
     *
     */
    fun generatePairwise(seed: String) {
        TODO("Not implemented")
    }

    /**
     * Generate a seed.
     */
    fun generateSeed(): String {
        TODO("Not implemented")
    }
}