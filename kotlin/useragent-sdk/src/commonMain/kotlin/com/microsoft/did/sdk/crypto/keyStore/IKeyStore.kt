package com.microsoft.did.sdk.crypto.keyStore

import com.microsoft.did.sdk.crypto.keys.KeyContainer
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.keys.SecretKey

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * Interface defining methods and properties to
 * be implemented by specific key stores.
 */
interface IKeyStore {
    /**
     * Returns the key associated with the specified
     * key reference.
     * @param keyReference for which to return the key.
     */
    fun getSecretKey(keyReference: String): KeyContainer<SecretKey>

    fun getPrivateKey(keyReference: String): KeyContainer<PrivateKey>

    fun getPublicKey(keyReference: String): KeyContainer<PublicKey>

    /**
     * Returns the key associated with the specified key id
     * @param keyIdentifier the key identifier to search for
     */
    fun getSecretKeyById(keyId: String): SecretKey?
    fun getPrivateKeyById(keyId: String): PrivateKey?
    fun getPublicKeyById(keyId: String): PublicKey?

    /**
     * Saves the specified key to the key store using
     * the key reference.
     * @param keyReference Reference for the key being saved.
     * @param key being saved to the key store.
     */
    fun save(keyReference: String, key: SecretKey): Unit
    fun save(keyReference: String, key: PrivateKey): Unit
    fun save(keyReference: String, key: PublicKey): Unit

    /**
     * Lists all key references with their corresponding key ids
     */
    fun list(): Map<String, KeyStoreListItem>
}