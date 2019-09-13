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
class SubtleCryptoFactory(default: SubtleCrypto) {

    /**
     * The key encryptors
     */
    var keyEncrypters: Map<String, SubtleCrypto> = mapOf("*" to default)

    /**
     * The shared key encryptors
     */
    var sharedKeyEncrypters: Map<String, SubtleCrypto> = mapOf("*" to default)

    /**
     * The symmetric content encryptors
     */
    var symmetricEncrypter: Map<String, SubtleCrypto> = mapOf("*" to default)

    /**
     * The message signer
     */
    var messageSigners: Map<String, SubtleCrypto> = mapOf("*" to default)

    /**
     * The hmac operations
     */
    var messageAuthenticationCodeSigners: Map<String, SubtleCrypto> = mapOf("*" to default)

    /**
     * The digest operations
     */
    var messageDigests: Map<String, SubtleCrypto> = mapOf("*" to default)

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
        return this.keyEncrypters.getOrElse(name, { this.keyEncrypters[defaultAlgorithm] ?: error("Default algorithm not implemented") });
    }

    /**
     * Gets the shared key encrypter object given the encryption algorithm's name
     * Used for DH algorithms
     * @param name The name of the algorithm
     * @returns The corresponding crypto API
     */
    fun getSharedKeyEncrypter (name: String): SubtleCrypto {
        return this.sharedKeyEncrypters.getOrElse(name, { this.sharedKeyEncrypters[defaultAlgorithm] ?: error("Default algorithm not implemented") });
    }

    /**
     * Gets the SymmetricEncrypter object given the symmetric encryption algorithm's name
     * @param name The name of the algorithm
     * @returns The corresponding crypto API
     */
    fun getSymmetricEncrypter (name: String): SubtleCrypto {
        return this.symmetricEncrypter.getOrElse(name, { this.symmetricEncrypter[defaultAlgorithm] ?: error("Default algorithm not implemented") });
    }

    /**
     * Gets the message signer object given the signing algorithm's name
     * @param name The name of the algorithm
     * @returns The corresponding crypto API
     */
    fun getMessageSigner (name: String): SubtleCrypto {
        return this.messageSigners.getOrElse(name, { this.messageSigners[defaultAlgorithm] ?: error("Default algorithm not implemented") });
    }

    /**
     * Gets the mac signer object given the signing algorithm's name
     * @param name The name of the algorithm
     * @returns The corresponding crypto API
     */
    fun getMessageAuthenticationCodeSigners (name: String): SubtleCrypto {
        return this.messageAuthenticationCodeSigners.getOrElse(name, { this.messageAuthenticationCodeSigners[defaultAlgorithm] ?: error("Default algorithm not implemented") });
    }

    /**
     * Gets the message digest object given the digest algorithm's name
     * @param name The name of the algorithm
     * @returns The corresponding crypto API
     */
    fun getMessageDigest (name: String): SubtleCrypto {
        return this.messageDigests.getOrElse(name, { this.messageDigests[defaultAlgorithm] ?: error("Default algorithm not implemented") });
    }
}