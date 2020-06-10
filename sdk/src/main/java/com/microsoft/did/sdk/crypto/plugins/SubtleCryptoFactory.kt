package com.microsoft.did.sdk.crypto.plugins

import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.util.controlflow.CryptoException

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * Utility class to handle all CryptoSuite dependency injection
 */
class SubtleCryptoFactory(default: SubtleCrypto) {

    private val defaultSubtleCryptoMapItem = SubtleCryptoMapItem(default, SubtleCryptoScope.ALL)

    /**
     * The key encryptors
     */
    var keyEncrypters: MutableMap<String, MutableList<SubtleCryptoMapItem>> = mutableMapOf("*" to mutableListOf(defaultSubtleCryptoMapItem))

    /**
     * The shared key encryptors
     */
    var sharedKeyEncrypters: MutableMap<String, MutableList<SubtleCryptoMapItem>> =
        mutableMapOf("*" to mutableListOf(defaultSubtleCryptoMapItem))

    /**
     * The symmetric content encryptors
     */
    var symmetricEncrypter: MutableMap<String, MutableList<SubtleCryptoMapItem>> =
        mutableMapOf("*" to mutableListOf(defaultSubtleCryptoMapItem))

    /**
     * The message signer
     */
    var messageSigners: MutableMap<String, MutableList<SubtleCryptoMapItem>> =
        mutableMapOf("*" to mutableListOf(defaultSubtleCryptoMapItem))

    /**
     * The hmac operations
     */
    var messageAuthenticationCodeSigners: MutableMap<String, MutableList<SubtleCryptoMapItem>> =
        mutableMapOf("*" to mutableListOf(defaultSubtleCryptoMapItem))

    /**
     * The digest operations
     */
    var messageDigests: MutableMap<String, MutableList<SubtleCryptoMapItem>> =
        mutableMapOf("*" to mutableListOf(defaultSubtleCryptoMapItem))

    /**
     * Label for default algorithm
     */
    private val defaultAlgorithm: String = "*"

    /**
     * Sets the key encrypter plugin given the encryption algorithm's name
     * @param name The name of the algorithm
     * @param cryptoSuiteMapItem Array containing subtle crypto API's and their scope
     */
    fun addKeyEncrypter(name: String, subtleCrypto: SubtleCryptoMapItem) {
        addOrCreateListFor(keyEncrypters, name, subtleCrypto)
    }

    /**
     * Gets the key encrypter object given the encryption algorithm's name
     * @param name The name of the algorithm
     * @returns The corresponding crypto API
     */
    fun getKeyEncrypter(name: String, scope: SubtleCryptoScope): SubtleCrypto {
        return getSubtleCryptoFrom(keyEncrypters, name, scope)
    }


    /**
     * Sets the shared key encrypter plugin given the encryption algorithm's name
     * @param name The name of the algorithm
     * @param cryptoSuiteMapItem Array containing subtle crypto API's and their scope
     */
    fun addSharedKeyEncrypter(name: String, subtleCrypto: SubtleCryptoMapItem) {
        addOrCreateListFor(sharedKeyEncrypters, name, subtleCrypto)
    }

    /**
     * Gets the shared key encrypter object given the encryption algorithm's name
     * Used for DH algorithms
     * @param name The name of the algorithm
     * @returns The corresponding crypto API
     */
    fun getSharedKeyEncrypter(name: String, scope: SubtleCryptoScope): SubtleCrypto {
        return getSubtleCryptoFrom(sharedKeyEncrypters, name, scope)
    }

    /**
     * Sets the SymmetricEncrypter object plugin given the encryption algorithm's name
     * @param name The name of the algorithm
     * @param cryptoSuiteMapItem Array containing subtle crypto API's and their scope
     */
    fun addSymmetricEncrypter(name: String, subtleCrypto: SubtleCryptoMapItem) {
        addOrCreateListFor(symmetricEncrypter, name, subtleCrypto)
    }

    /**
     * Gets the SymmetricEncrypter object given the symmetric encryption algorithm's name
     * @param name The name of the algorithm
     * @returns The corresponding crypto API
     */
    fun getSymmetricEncrypter(name: String, scope: SubtleCryptoScope): SubtleCrypto {
        return getSubtleCryptoFrom(symmetricEncrypter, name, scope)
    }

    /**
     * Sets the message signer object plugin given the encryption algorithm's name
     * @param name The name of the algorithm
     * @param cryptoSuiteMapItem Array containing subtle crypto API's and their scope
     */
    fun addMessageSigner(name: String, subtleCrypto: SubtleCryptoMapItem) {
        addOrCreateListFor(messageSigners, name, subtleCrypto)
    }


    /**
     * Gets the message signer object given the signing algorithm's name
     * @param name The name of the algorithm
     * @returns The corresponding crypto API
     */
    fun getMessageSigner(name: String, scope: SubtleCryptoScope): SubtleCrypto {
        return getSubtleCryptoFrom(messageSigners, name, scope)
    }

    /**
     * Sets the mmac signer object plugin given the encryption algorithm's name
     * @param name The name of the algorithm
     * @param cryptoSuiteMapItem Array containing subtle crypto API's and their scope
     */
    fun addMessageAuthenticationCodeSigner(name: String, subtleCrypto: SubtleCryptoMapItem) {
        addOrCreateListFor(messageAuthenticationCodeSigners, name, subtleCrypto)
    }

    /**
     * Gets the mac signer object given the signing algorithm's name
     * @param name The name of the algorithm
     * @returns The corresponding crypto API
     */
    fun getMessageAuthenticationCodeSigners(name: String, scope: SubtleCryptoScope): SubtleCrypto {
        return getSubtleCryptoFrom(messageAuthenticationCodeSigners, name, scope)
    }

    /**
     * Sets the message digest object plugin given the encryption algorithm's name
     * @param name The name of the algorithm
     * @param cryptoSuiteMapItem Array containing subtle crypto API's and their scope
     */
    fun addMessageDigest(name: String, subtleCrypto: SubtleCryptoMapItem) {
        addOrCreateListFor(messageDigests, name, subtleCrypto)
    }

    /**
     * Gets the message digest object given the digest algorithm's name
     * @param name The name of the algorithm
     * @returns The corresponding crypto API
     */
    fun getMessageDigest(name: String, scope: SubtleCryptoScope): SubtleCrypto {
        return getSubtleCryptoFrom(messageDigests, name, scope)
    }

    fun getBestMatch(name: String, scope: SubtleCryptoScope): SubtleCrypto {
        fun findMatch(map: MutableMap<String, MutableList<SubtleCryptoMapItem>>, name: String, scope: SubtleCryptoScope): SubtleCrypto? {
            val list = map[name]
            if (list != null) {
                try {
                    return findSubtleCryptoFor(list, scope)
                } catch (error: Throwable) {
                    // no match
                }
            }
            return null
        }
        // look in the order of:
        val searchSequence =
            listOf(keyEncrypters, messageSigners, symmetricEncrypter, messageDigests, sharedKeyEncrypters, messageAuthenticationCodeSigners)

        searchSequence.forEach {
            val match = findMatch(it, name, scope)
            if (match != null) {
                return match
            }
        }

        return defaultSubtleCryptoMapItem.subtleCrypto
    }

    private fun addOrCreateListFor(
        map: MutableMap<String, MutableList<SubtleCryptoMapItem>>,
        name: String,
        subtleCrypto: SubtleCryptoMapItem
    ) {
        val list = map[name]
        if (list != null) {
            list.add(subtleCrypto)
            map[name] = list
        } else {
            map[name] = mutableListOf(subtleCrypto)
        }
    }

    private fun getSubtleCryptoFrom(
        map: MutableMap<String, MutableList<SubtleCryptoMapItem>>,
        name: String,
        scope: SubtleCryptoScope
    ): SubtleCrypto {
        val list = map[name]
        // check for the specified scope
        if (list != null) {
            try {
                return findSubtleCryptoFor(list, scope)
            } catch (error: Throwable) {
                // oh darn we didn't find one in the specified algorithm :'(
            }
        }
        return findSubtleCryptoFor(map[defaultAlgorithm]!!, scope)
    }

    private fun findSubtleCryptoFor(list: List<SubtleCryptoMapItem>, scope: SubtleCryptoScope): SubtleCrypto {
        val exactScope = list.filter { it.scope == scope }
        if (exactScope.isNotEmpty()) {
            return exactScope.first().subtleCrypto
        } else if (scope != SubtleCryptoScope.ALL) {
            val closeEnoughScope = list.filter { it.scope == SubtleCryptoScope.ALL }
            if (closeEnoughScope.isNotEmpty()) {
                return closeEnoughScope.first().subtleCrypto
            }
        }
        throw CryptoException("Could not find SubtleCrypto of appropriate scope")
    }
}