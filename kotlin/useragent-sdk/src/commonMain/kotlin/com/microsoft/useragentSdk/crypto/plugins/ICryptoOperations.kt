package com.microsoft.useragentSdk.crypto.plugins

import com.microsoft.useragentSdk.crypto.models.webCryptoApi.SubtleCrypto

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * Interface for the Crypto Algorithms Plugins
 */
abstract class CryptoOperations {
    /**
     * Gets all of the key encryption Algorithms from the plugin
     * @returns a subtle crypto object for key encryption/decryption
     */
    abstract fun getKeyEncrypters(): SubtleCrypto;

    /**
     * Gets all of the key sharing encryption Algorithms from the plugin
     * @returns a subtle crypto object for key sharing encryption/decryption
     */
    abstract fun getSharedKeyEncrypters(): SubtleCrypto;

    /**
     * Get all of the symmetric encrypter algorithms from the plugin
     * @returns a subtle crypto object for symmetric encryption/decryption
     */
    abstract fun getSymmetricEncrypters(): SubtleCrypto;

    /**
     * Gets all of the message signing Algorithms from the plugin
     * @returns a subtle crypto object for message signing
     */
    abstract fun getMessageSigners(): SubtleCrypto;

    /**
     * Gets all of the message authentication code signing Algorithms from the plugin.
     * Will be used for primitive operations such as key generation.
     * @returns a subtle crypto object for message signing
     */
    abstract fun messageAuthenticationCodeSigners(): SubtleCrypto;

    /**
     * Gets all of the message digest Algorithms from the plugin.
     * @returns a subtle crypto object for message digests
     */
    abstract fun getMessageDigests(): SubtleCrypto;

    /**
     * Returns the @class SubtleCrypto ipmplementation for the current environment
     */
    abstract fun getSubtleCrypto(): SubtleCrypto;
}