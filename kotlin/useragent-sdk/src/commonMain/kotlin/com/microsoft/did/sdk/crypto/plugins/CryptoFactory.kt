package com.microsoft.did.sdk.crypto.plugins

import com.microsoft.did.sdk.crypto.keyStore.IKeyStore
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * Utility class to handle all CryptoSuite dependency injection
 */
class CryptoFactory(keyStore: IKeyStore, crypto: CryptoOperations) {

    /**
     * The key encryptors
     */
    var keyEncrypters: Map<String, CryptoOperations> = mapOf("*" to crypto)

    /**
     * The shared key encryptors
     */
    var sharedKeyEncrypters: Map<String, CryptoOperations> = mapOf("*" to crypto)

    /**
     * The symmetric content encryptors
     */
    var symmetricEncrypter: Map<String, CryptoOperations> = mapOf("*" to crypto)

    /**
     * The message signer
     */
    var messageSigners: Map<String, CryptoOperations> = mapOf("*" to crypto)

    /**
     * The hmac operations
     */
    var messageAuthenticationCodeSigners: Map<String, CryptoOperations> = mapOf("*" to crypto)

    /**
     * The digest operations
     */
    var messageDigests: Map<String, CryptoOperations> = mapOf("*" to crypto)

    /**
     * Key store used by the CryptoFactory
     */
    var keyStore: IKeyStore = keyStore

    /**
     * Label for default algorithm
     */
    private val defaultAlgorithm: String = "*"

    /**
     * Gets the key encrypter object given the encryption algorithm's name
     * @param name The name of the algorithm
     * @returns The corresponding crypto API
     */
    fun getKeyEncrypter (name: String): SubtleCrypto {
        if (this.keyEncrypters.containsKey(name)) {
            return (this.keyEncrypters[name] ?: error("")).getKeyEncrypters();
        }
        return (this.keyEncrypters[this.defaultAlgorithm] ?: error("Default algorithm not implemented " +
                "for key encrypters")).getKeyEncrypters();
    }

    /**
     * Gets the shared key encrypter object given the encryption algorithm's name
     * Used for DH algorithms
     * @param name The name of the algorithm
     * @returns The corresponding crypto API
     */
    fun getSharedKeyEncrypter (name: String): SubtleCrypto {
        if (this.sharedKeyEncrypters.containsKey(name)) {
            return (this.sharedKeyEncrypters[name] ?: error("")).getSharedKeyEncrypters();
        }
        return (this.sharedKeyEncrypters[this.defaultAlgorithm] ?: error("Default algorithm not implemented " +
                "for shared key encrypters")).getSharedKeyEncrypters();
    }

    /**
     * Gets the SymmetricEncrypter object given the symmetric encryption algorithm's name
     * @param name The name of the algorithm
     * @returns The corresponding crypto API
     */
    fun getSymmetricEncrypter (name: String): SubtleCrypto {
        if (this.symmetricEncrypter.containsKey(name)) {
            return (this.symmetricEncrypter[name] ?: error("")).getSymmetricEncrypters();
        }
        return (this.symmetricEncrypter[this.defaultAlgorithm] ?: error("Default algorithm not implemented for " +
                "symmetric encrypters")).getSymmetricEncrypters();
    }

    /**
     * Gets the message signer object given the signing algorithm's name
     * @param name The name of the algorithm
     * @returns The corresponding crypto API
     */
    fun getMessageSigner (name: String): SubtleCrypto {
        if (this.messageSigners.containsKey(name)) {
            return (this.messageSigners[name] ?: error("")).getMessageSigners();
        }
        return (this.messageSigners[this.defaultAlgorithm] ?: error("Default algorithm not implemented for " +
                "message signers")).getMessageSigners();
    }

    /**
     * Gets the mac signer object given the signing algorithm's name
     * @param name The name of the algorithm
     * @returns The corresponding crypto API
     */
    fun getMessageAuthenticationCodeSigners (name: String): SubtleCrypto {
        if (this.messageAuthenticationCodeSigners.containsKey(name)) {
            return (this.messageAuthenticationCodeSigners[name] ?: error("")).messageAuthenticationCodeSigners();
        }
        return (this.messageAuthenticationCodeSigners[this.defaultAlgorithm] ?: error("Default algorithm not " +
                "implemented for message authentication code signers")).messageAuthenticationCodeSigners();
    }

    /**
     * Gets the message digest object given the digest algorithm's name
     * @param name The name of the algorithm
     * @returns The corresponding crypto API
     */
    fun getMessageDigest (name: String): SubtleCrypto {
        if (this.messageDigests.containsKey(name)) {
            return this.messageDigests[name]!!.getMessageDigests();
        }
        return (this.messageDigests[this.defaultAlgorithm] ?: error("Default algorithm not implemented " +
                "for message digests")).getMessageDigests();
    }
}